package com.ceos.bankids.service;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.KidBackupDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KidBackupServiceImpl implements KidBackupService {

    @Override
    @Transactional
    public KidBackupDTO backupKidUser(User user) {
        return null;
    }

}
