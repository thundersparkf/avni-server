package org.avni.service;

import org.joda.time.DateTime;
import org.avni.dao.OperatingIndividualScopeAwareRepository;
import org.avni.dao.SyncParameters;
import org.avni.dao.VirtualCatchmentRepository;
import org.avni.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static org.avni.domain.OperatingIndividualScope.ByCatchment;
import static org.avni.domain.OperatingIndividualScope.ByFacility;

@Service
public class ScopeBasedSyncService<T extends CHSEntity> {
    private AddressLevelService addressLevelService;

    public ScopeBasedSyncService(AddressLevelService addressLevelService) {
        this.addressLevelService = addressLevelService;
    }

    public Page<T> getSyncResult(OperatingIndividualScopeAwareRepository<T> repository, User user, DateTime lastModifiedDateTime, DateTime now, Long filter, Pageable pageable) {
        List<Long> addressLevels = addressLevelService.getAllAddressLevelIdsForCatchment(user.getCatchment());

        OperatingIndividualScope scope = user.getOperatingIndividualScope();
        Facility userFacility = user.getFacility();
        Catchment catchment = user.getCatchment();
        if (ByCatchment.equals(scope)) {
            return repository.syncByCatchment(new SyncParameters(0, catchment.getId(), lastModifiedDateTime, now, filter, pageable, addressLevels));
        }
        if (ByFacility.equals(scope)) {
            return repository.syncByFacility(new SyncParameters(userFacility.getId(), 0, lastModifiedDateTime, now, filter, pageable, addressLevels));
        }
        return new PageImpl<>(Collections.emptyList());
    }
}
