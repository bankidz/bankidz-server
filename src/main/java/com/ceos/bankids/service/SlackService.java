package com.ceos.bankids.service;

import org.springframework.stereotype.Service;

@Service
public interface SlackService {

    public void sendWithdrawalMessage(String user, Long id, String message);
}
