package com.ceos.bankids.service;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ParentBackupDTO;
import org.springframework.stereotype.Service;

@Service
public interface ParentBackupService {

    public ParentBackupDTO backupParentUser(User user);
}
