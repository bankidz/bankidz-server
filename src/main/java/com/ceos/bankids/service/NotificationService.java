package com.ceos.bankids.service;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.FcmMessageDTO;
import org.springframework.stereotype.Service;

@Service
public interface NotificationService {

    public void makeChallengeStatusMessage(FcmMessageDTO fcmMessageDTO,
        User authUser);
}
