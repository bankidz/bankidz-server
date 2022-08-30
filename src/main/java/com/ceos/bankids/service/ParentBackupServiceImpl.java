package com.ceos.bankids.service;

import com.ceos.bankids.domain.ParentBackup;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ParentBackupDTO;
import com.ceos.bankids.repository.ParentBackupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParentBackupServiceImpl implements ParentBackupService {

    private final ParentBackupRepository parentBackupRepository;

    @Override
    @Transactional
    public ParentBackupDTO backupParentUser(User user) {
        ParentBackup parentBackup = ParentBackup.builder()
            .birthYear(user.getBirthday().substring(0, 4))
            .isKid(user.getIsKid())
            .acceptedRequest(user.getParent().getAcceptedRequest())
            .totalRequest(user.getParent().getTotalRequest())
            .build();
        parentBackupRepository.save(parentBackup);

        return new ParentBackupDTO(parentBackup);
    }

}
