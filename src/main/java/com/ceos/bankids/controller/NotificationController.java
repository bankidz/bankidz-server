package com.ceos.bankids.controller;

import com.ceos.bankids.constant.ChallengeStatus;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.service.ExpoNotificationServiceImpl;
import io.swagger.annotations.ApiOperation;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final ExpoNotificationServiceImpl expoNotificationService;

    @Async
    @ApiOperation(value = "돈길 상태 변경 알림")
    @GetMapping(produces = "application/json; charset=utf-8")
    public void notification(Challenge challenge, User authUser) {
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
        log.info("유저 {}의 돈길 {}의 {} 상태변경 알림", authUser.getId(), challenge.getId(),
            challenge.getChallengeStatus());
    }

    @Async
    @ApiOperation(value = "유저 레벨업 직전 알림")
    public void userLevelUpMinusOne(User authUser) {

        String title = "나의 레벨 보기";
        String notificationBody = "레벨업까지 단 한 개만 완주하면 돼요";
        String token = "ExponentPushToken[Gui56sA2O6WAb839ZEH0uI]";
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("userId", authUser.getId());
        expoNotificationService.sendMessage(token, title, notificationBody, newMap);
        log.info("유저 id = {}의 레벨업 직전 알림", authUser.getId());
    }

    @Async
    @ApiOperation(value = "유저 레벨업 절반 달성 알림")
    public void userLevelUpHalf(User authUser) {

        String title = "나의 레벨 보기";
        String notificationBody = "레벨업까지 절반이나 왔어요.. 힘내세요!";
        String token = "ExponentPushToken[Gui56sA2O6WAb839ZEH0uI]";
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("userId", authUser.getId());
        expoNotificationService.sendMessage(token, title, notificationBody, newMap);
        log.info("유저 id = {}의 레벨업 절반 달성 알림", authUser.getId());
    }

    @Async
    @ApiOperation(value = "자녀가 돈길 제안했을 때 부모 알림")
    public void createPendingChallengeNotification(User contractUser, ChallengeUser challengeUser) {

        String title = "제안된 돈길 보기";
        String notificationBody = challengeUser.getUser().getUsername() + "님이 돈길을 제안했어요! 확인하러가기";
        String token = "ExponentPushToken[Gui56sA2O6WAb839ZEH0uI]";
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("user", challengeUser.getUser().getId());
        newMap.put("challenge", challengeUser.getChallenge().getId());
        expoNotificationService.sendMessage(token, title, notificationBody, newMap);
        log.info("부모 유저 id = {}에게 유저 id = {} 돈길 id = {} 의 돈길 제안", contractUser.getId(),
            challengeUser.getUser().getId(), challengeUser.getChallenge().getId());
    }

    @Async
    @ApiOperation(value = "자녀가 돈길을 걸었을 때 부모 알림")
    public void runProgressNotification(User contractUser, ChallengeUser challengeUser) {

        String title = "자녀의 돈길 걷기 확인";
        String notificationBody = challengeUser.getUser().getUsername() + "님이 지금 돈길을 걸었어요! 확인하러가기";
        String token = "ExponentPushToken[Gui56sA2O6WAb839ZEH0uI]";
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("user", challengeUser.getUser().getId());
        newMap.put("challenge", challengeUser.getChallenge().getId());
        expoNotificationService.sendMessage(token, title, notificationBody, newMap);
        log.info("부모 유저 id = {}에게 유저 id = {}의 돈길 id = {} 돈길 걷기 알림 전송", contractUser.getId(),
            challengeUser.getUser().getId(), challengeUser.getChallenge().getId());
    }

    @Async
    @ApiOperation(value = "돈길을 완주했을 때 부모 알림")
    public void achieveChallengeNotification(User contractUser, ChallengeUser challengeUser) {

        String title = "자녀의 돈길 완주";
        String notificationBody =
            "자녀가 드디어 돈길을 완주했어요 \n그동안 고생한 " + challengeUser.getUser().getUsername()
                + "님에게 이자로 보상해주세요!";
        String token = "ExponentPushToken[Gui56sA2O6WAb839ZEH0uI]";
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("user", challengeUser.getUser().getId());
        newMap.put("challenge", challengeUser.getChallenge().getId());
        expoNotificationService.sendMessage(token, title, notificationBody, newMap);
        log.info("부모 유저 id = {}에게 유저 id = {}의 돈길 id = {} 돈길 완주 알림 전송", contractUser.getId(),
            challengeUser.getUser().getId(), challengeUser.getChallenge().getId());
    }

    @Async
    @ApiOperation(value = "자녀 레벨업 시 부모 알림")
    public void kidLevelUpNotification(User contractUser, User user, Long level, Long afterLevel) {

        String title = "자녀의 레벨업!";
        String notificationBody =
            user.getUsername() + "님이 레벨" + level + "에서 " + afterLevel + "로 올랐어요! \n확인하고 응원하러가기";
        String token = "ExponentPushToken[Gui56sA2O6WAb839ZEH0uI]";
        HashMap<String, Object> newMap = new HashMap<>();
        newMap.put("user", user.getId());
        expoNotificationService.sendMessage(token, title, notificationBody, newMap);
        log.info("부모 유저 id = {}에게 유저 id = {}의 레벨업 알림 전송", contractUser.getId(), user.getId());
    }
}
