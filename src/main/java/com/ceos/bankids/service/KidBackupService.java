package com.ceos.bankids.service;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.KidBackupDTO;
import org.springframework.stereotype.Service;

@Service
public interface KidBackupService {

    public KidBackupDTO backupKidUser(User user);

}
