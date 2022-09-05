package com.ceos.bankids.service;

import com.ceos.bankids.dto.NoticeDTO;
import org.springframework.stereotype.Service;

@Service
public interface NoticeService {

    public void postNotice(String title, String body);

    public NoticeDTO readNotice(Long noticeId);
}
