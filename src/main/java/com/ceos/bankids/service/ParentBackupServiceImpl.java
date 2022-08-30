package com.ceos.bankids.service;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ParentBackupDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParentBackupServiceImpl implements ParentBackupService {

    @Override
    @Transactional
    public ParentBackupDTO backupParentUser(User user) {
        return null;
    }

}
