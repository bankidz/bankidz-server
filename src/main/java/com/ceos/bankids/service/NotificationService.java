package com.ceos.bankids.service;

import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.User;
import org.springframework.stereotype.Service;

@Service
public interface NotificationService {

    public void makeChallengeStatusMessage(Challenge challenge,
        User authUser);
}
