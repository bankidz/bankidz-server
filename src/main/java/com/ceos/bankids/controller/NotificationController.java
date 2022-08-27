package com.ceos.bankids.controller;

import com.ceos.bankids.constant.ChallengeStatus;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.service.ExpoNotificationServiceImpl;
import com.ceos.bankids.service.NotificationServiceImpl;
import io.swagger.annotations.ApiOperation;
import java.util.HashMap;
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
    private final ExpoNotificationServiceImpl expoNotificationService;

    @ApiOperation(value = "돈길 상태 변경 알림")
    @GetMapping(produces = "application/json; charset=utf-8")
    public String notification(Challenge challenge, User authUser) {
//        notificationService.makeChallengeStatusMessage(challenge, authUser);
        String title = "돈길 상태가 변경되었어요!";
        String notificationBody =
            challenge.getChallengeStatus() == ChallengeStatus.WALKING ? "제안된 돈길이 수락되었어요!"
                : "제안된 돈길이 거절당했어요. 이유를 알아봐요.";
        String token = "ExponentPushToken[EQBviQMfJm_1riRkM0KdjP]";
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("challengeId", challenge.getId());
        newMap.put("userId", authUser.getId());
        expoNotificationService.sendMessage(token, title, notificationBody, newMap);
        return "NOTIFICATION SUCCESS";
    }
}
