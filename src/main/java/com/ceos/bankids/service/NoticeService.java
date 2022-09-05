package com.ceos.bankids.service;

import com.ceos.bankids.dto.NoticeDTO;
import com.ceos.bankids.dto.NoticeListDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface NoticeService {

    public NoticeDTO postNotice(String title, String body);

    public NoticeDTO readNotice(Long noticeId);

    public List<NoticeListDTO> readNoticeList();

}
