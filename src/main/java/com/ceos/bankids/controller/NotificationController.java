package com.ceos.bankids.controller;

import com.ceos.bankids.config.CommonResponse;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.NotificationDTO;
import com.ceos.bankids.dto.NotificationIsReadDTO;
import com.ceos.bankids.dto.NotificationListDTO;
import com.ceos.bankids.mapper.NotificationMapper;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationMapper notificationMapper;

    @ApiOperation(value = "유저 알림 리스트 가져오기")
    @GetMapping(produces = "application/json; charset=utf-8")
    public CommonResponse<NotificationListDTO> getNotificationList(
        @AuthenticationPrincipal User authUser, @RequestParam(required = false) Long lastId) {

        log.info("api = 유저 알림 리스트 가져오기 user = {}", authUser.getUsername());
        NotificationListDTO notificationListDTOS = notificationMapper.readNotificationListMapper(
            authUser, lastId);
        return CommonResponse.onSuccess(notificationListDTOS);
    }

    @ApiOperation(value = "유저 안읽은 알림 있는지 확인")
    @GetMapping(value = "/isRead", produces = "application/json; charset=utf-8")
    public CommonResponse<NotificationIsReadDTO> getNotificationIsAllRead(
        @AuthenticationPrincipal User authUser) {

        log.info("api = 안읽은 알림 있는지 확인 user = {}", authUser.getId());
        NotificationIsReadDTO notificationIsReadDTO = notificationMapper.readNotificationIsAllReadMapper(
            authUser);
        return CommonResponse.onSuccess(notificationIsReadDTO);
    }


    @ApiOperation(value = "유저 알림 읽음 확인")
    @PatchMapping(value = "/{notificationId}", produces = "application/json; charset=utf-8")
    public CommonResponse<NotificationDTO> patchNotification(@AuthenticationPrincipal User authUser,
        @PathVariable Long notificationId) {

        log.info("api = 유저 알림 읽음 처리 user = {} notification = {}", authUser.getUsername(),
            notificationId);
        NotificationDTO notificationDTO = notificationMapper.updateNotificationMapper(authUser,
            notificationId);
        return CommonResponse.onSuccess(notificationDTO);
    }
}
