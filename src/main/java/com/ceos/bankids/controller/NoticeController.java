package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.controller.request.NoticeRequest;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.AllSendNotificationDTO;
import com.ceos.bankids.dto.NoticeDTO;
import com.ceos.bankids.dto.NoticeListDTO;
import com.ceos.bankids.exception.ForbiddenException;
import com.ceos.bankids.service.NoticeServiceImpl;
import io.swagger.annotations.ApiOperation;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeServiceImpl noticeService;
    private final NotificationController notificationController;

    @ApiOperation(value = "공지사항 작성")
    @PostMapping(produces = "application/json; charset=utf-8")
    public CommonResponse<NoticeDTO> postNotice(@AuthenticationPrincipal User authUser,
        NoticeRequest noticeRequest) {

        log.info("api = 공지사항 작성");
        if (authUser.getId() != 9L) {
            throw new ForbiddenException(ErrorCode.NOTICE_AUTH_ERROR.getErrorCode());
        }
        String title = noticeRequest.getTitle();
        String body = noticeRequest.getBody();
        String message = noticeRequest.getMessage();
        HashMap<String, Object> newMap = new HashMap<>();

        NoticeDTO noticeDTO = noticeService.postNotice(title, body);
        newMap.put("noticeId", noticeDTO.getId());
        AllSendNotificationDTO allSendNotificationDTO = new AllSendNotificationDTO(title, message,
            newMap);
        notificationController.allSendNotification(allSendNotificationDTO, authUser);

        return CommonResponse.onSuccess(noticeDTO);
    }

    @ApiOperation(value = "공지사항 가져오기")
    @GetMapping(value = "/{noticeId}", produces = "application/json; charset=utf-8")
    public CommonResponse<NoticeDTO> getNotice(@AuthenticationPrincipal User authUser,
        @PathVariable Long noticeId) {

        log.info("api = 공지사항 가져오기 user = {}, notice = {}", authUser.getUsername(), noticeId);
        NoticeDTO noticeDTO = noticeService.readNotice(noticeId);

        return CommonResponse.onSuccess(noticeDTO);
    }

    @ApiOperation(value = "공지사항 리스트 가져오기")
    @GetMapping(produces = "application/json; charset=utf-8")
    public CommonResponse<List<NoticeListDTO>> getNoticeList(
        @AuthenticationPrincipal User authUser) {

        log.info("api = 공지사항 리스트 가져오기 user = {}", authUser.getUsername());
        List<NoticeListDTO> noticeDTOList = noticeService.readNoticeList();

        return CommonResponse.onSuccess(noticeDTOList);
    }
}
