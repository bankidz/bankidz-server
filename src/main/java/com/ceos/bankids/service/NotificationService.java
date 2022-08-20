package com.ceos.bankids.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

@Service
public interface NotificationService {

    String makeChallengeStatusMessage(String token, String title, String body, String path)
        throws JsonProcessingException;
}
