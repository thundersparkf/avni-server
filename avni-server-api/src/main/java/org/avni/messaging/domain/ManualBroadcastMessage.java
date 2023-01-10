package org.avni.messaging.domain;

import org.avni.server.domain.OrganisationAwareEntity;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "manual_broadcast_message")
public class ManualBroadcastMessage extends OrganisationAwareEntity {
    @Column
    private String messageTemplateId;

    @Column(columnDefinition = "text[]")
    @Type(type = "parameters")
    private String[] parameters;

    public ManualBroadcastMessage(String messageTemplateId, String[] parameters) {
        this.messageTemplateId = messageTemplateId;
        this.parameters = parameters;
    }

    public ManualBroadcastMessage() {
    }

    public String[] getParameters() {
        return parameters;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    public String getMessageTemplateId() {
        return messageTemplateId;
    }

    public void setMessageTemplateId(String messageTemplateId) {
        this.messageTemplateId = messageTemplateId;
    }
}
