# <makefile>
# Objects: db, schema, server, package, env (code environment)
# Actions: clean, build, deploy, test
help:
	@IFS=$$'\n' ; \
	help_lines=(`fgrep -h "##" $(MAKEFILE_LIST) | fgrep -v fgrep | sed -e 's/\\$$//'`); \
	for help_line in $${help_lines[@]}; do \
	    IFS=$$'#' ; \
	    help_split=($$help_line) ; \
	    help_command=`echo $${help_split[0]} | sed -e 's/^ *//' -e 's/ *$$//'` ; \
	    help_info=`echo $${help_split[2]} | sed -e 's/^ *//' -e 's/ *$$//'` ; \
	    printf "%-30s %s\n" $$help_command $$help_info ; \
	done
# </makefile>


su:=$(shell id -un)


# <postgres>
_clean_db:
	-psql -h localhost -U $(su) postgres -c "SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = '$(database)' AND pid <> pg_backend_pid()"
	-psql -h localhost -U $(su) postgres -c 'drop database $(database)';

_build_db:
	-psql -h localhost -U $(su) postgres -c 'create database $(database) with owner openchs';
	-psql -h localhost -U $(su) postgres -c "create user $(database) with password 'password'";
	-psql -h localhost $(database) -c 'create extension if not exists "uuid-ossp"';
# </postgres>

# <db>
clean_db: ## Drops the database
	make _clean_db database=openchs

build_db: ## Creates new empty database
	make _build_db database=openchs

rebuild_db: clean_db build_db ## clean + build db
# </db>

# <testdb>
clean_testdb: ## Drops the test database
	make _clean_db database=openchs_test

build_testdb: ## Creates new empty database of test database
	make _build_db database=openchs_test

rebuild_testdb: clean_testdb build_testdb ## clean + build test db
# </testdb>


# <schema>
clean_schema: ## drops the schema
	flyway -user=openchs -password=password -url=jdbc:postgresql://localhost:5432/openchs -schemas=openchs clean

deploy_schema: ## Runs all migrations to create the schema with all the objects
	flyway -user=openchs -password=password -url=jdbc:postgresql://localhost:5432/openchs -schemas=openchs -locations=filesystem:../openchs-server/openchs-server-api/src/main/resources/db/migration/ migrate

redeploy_schema: clean_schema deploy_schema ## clean and deploy schema
# </schema>


# <server>
start_server: build_server ## Builds and starts the server
	mvn spring-boot:run
#	java -jar openchs-server-api/target/openchs-server-api-0.1-SNAPSHOT.jar

build_server: ## Builds the jar file
	mvn clean compile test-compile
	mvn install -DskipTests

deploy: ## Deploys the jar
	mvn clean compile test-compile
	mvn deploy -DskipTests


test_server: rebuild_testdb build_server ## Run tests
	mvn install
# <server>


#build: stop
#	@echo "Building all containers"
#	docker-compose rm -f
#	@echo "Building all containers"
#	docker-compose build
#release:
#	@echo "Building all containers"
#	docker-compose push httpd
#stop:
#	@echo "Stopping all services"
#	docker-compose stop
#	docker-compose kill
#start:
#	@echo "Starting all services"
#	docker-compose up
#restart: stop start