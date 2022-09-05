package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.NoticeDTO;
import com.ceos.bankids.service.NoticeServiceImpl;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeServiceImpl noticeService;

    @ApiOperation(value = "공지사항 가져오기")
    @GetMapping(value = "/{noticeId}", produces = "application/json; charset=utf-8")
    public CommonResponse<NoticeDTO> getNotice(@AuthenticationPrincipal User authUser,
        @PathVariable Long noticeId) {

        log.info("api = 공지사항 가져오기 user = {}, notice = {}", authUser.getUsername(), noticeId);
        NoticeDTO noticeDTO = noticeService.readNotice(noticeId);

        return CommonResponse.onSuccess(noticeDTO);
    }
}
