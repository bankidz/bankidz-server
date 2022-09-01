package com.ceos.bankids.service;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class SlackServiceImpl implements SlackService {

    @Value("${withdrawal.slack.webhook-uri}")
    private String SLACK_WITHDRAWAL_URI;

    @Override
    public void sendWithdrawalMessage(String user, Long id, String message) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = new HashMap<>();
        request.put("username", user + id);
        request.put("text", message);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request);

        restTemplate.exchange(SLACK_WITHDRAWAL_URI, HttpMethod.POST, entity, String.class);
    }
}
