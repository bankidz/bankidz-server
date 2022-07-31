package com.ceos.bankids.service;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.ProgressDTO;
import org.springframework.stereotype.Service;

@Service
public interface ProgressService {

    public ProgressDTO updateProgress(User user, Long challengeId);

}
