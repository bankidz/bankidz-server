package com.ceos.bankids.service;

import com.ceos.bankids.dto.FcmMessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

@Service
public interface NotificationService {

    String makeChallengeStatusMessage(FcmMessageDTO fcmMessageDTO)
        throws JsonProcessingException;
}
