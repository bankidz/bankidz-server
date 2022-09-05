package com.ceos.bankids.service;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.domain.Notice;
import com.ceos.bankids.dto.NoticeDTO;
import com.ceos.bankids.exception.BadRequestException;
import com.ceos.bankids.repository.NoticeRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional
    @Override
    public NoticeDTO postNotice(String title, String body) {

        Notice notice = Notice.builder().title(title).body(body).build();
        noticeRepository.save(notice);

        return new NoticeDTO(notice);
    }

    @Transactional
    @Override
    public NoticeDTO readNotice(Long noticeId) {

        Notice notice = noticeRepository.findById(noticeId).orElseThrow(
            () -> new BadRequestException(ErrorCode.NOT_EXIST_NOTICE_ERROR.getErrorCode()));

        return new NoticeDTO(notice);
    }

    @Transactional
    @Override
    public List<NoticeDTO> readNoticeList() {

        return noticeRepository.findAll().stream()
            .map(NoticeDTO::new)
            .collect(Collectors.toList());
    }


}
