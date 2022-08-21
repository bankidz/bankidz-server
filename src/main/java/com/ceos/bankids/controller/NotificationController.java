package com.ceos.bankids.controller;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.dto.FcmMessageDTO;
import com.ceos.bankids.service.NotificationServiceImpl;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationServiceImpl notificationService;

    @ApiOperation(value = "돈길 상태 변경 알림")
    @GetMapping(produces = "application/json; charset=utf-8")
    public String notification(FcmMessageDTO fcmMessageDTO, User authUser) {
        notificationService.makeChallengeStatusMessage(fcmMessageDTO, authUser);
        return "NOTIFICATION SUCCESS";
    }
}
