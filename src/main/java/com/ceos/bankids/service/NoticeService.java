package com.ceos.bankids.service;

import org.springframework.stereotype.Service;

@Service
public interface NoticeService {

    public void postNotice(String title, String body);
}
