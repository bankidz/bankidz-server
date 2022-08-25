package com.ceos.bankids.service;

import com.ceos.bankids.constant.ChallengeStatus;
import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.ChallengeNotification;
import com.ceos.bankids.domain.ChallengeUser;
import com.ceos.bankids.domain.User;
import com.ceos.bankids.exception.InternalServerException;
import com.ceos.bankids.repository.ChallengeNotificationRepository;
import com.ceos.bankids.repository.ChallengeUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final ChallengeNotificationRepository challengeNotificationRepository;
    private final ChallengeUserRepository challengeUserRepository;
    private final ObjectMapper objectMapper;
    @Value("${fcm.key.path}")
    private String FCM_PRIVATE_KEY_PATH;
    @Value("${fcm.key.scope}")
    private String fireBaseScope;
    private FirebaseMessaging firebaseMessagingInstance;

    // DI 할 때, 생성자 돌려서 초기화
    @PostConstruct
    private void fireBaseInit() throws IOException {
        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(
                    new ClassPathResource(FCM_PRIVATE_KEY_PATH).getInputStream())
                .createScoped(List.of(fireBaseScope))).build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(firebaseOptions);
            log.info("Firebase successfully run");
        }
    }

    // firebase 서버에 전달할 메시지 생성
    @Async
    @Override
    public void makeChallengeStatusMessage(Challenge challenge,
        User user) {
        String notificationBody =
            challenge.getChallengeStatus() == ChallengeStatus.WALKING ? "제안된 돈길이 수락되었어요!"
                : "제안된 돈길이 거절당했어요. 이유를 알아봐요.";
        Notification notification = new Notification("돈길 상태가 변경되었어요!", notificationBody);
        HashMap<String, String> dataMap = new HashMap<>();
        dataMap.put("challenge", challenge.getId().toString());
        dataMap.put("challengeStatus", challenge.getChallengeStatus().toString());
        Message message = Message.builder().setNotification(notification).setToken("token")
            .putAllData(dataMap).build();
        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.error("service err={}", e);
            throw new InternalServerException(ErrorCode.NOTIFICATION_SERVICE_ERROR.getErrorCode());
        }
        ChallengeUser challengeUser = challengeUserRepository.findByChallengeId(
            challenge.getId()).orElseThrow(
            () -> new InternalServerException(ErrorCode.NOTIFICATION_MESSAGE_ERROR.getErrorCode()));
        ChallengeNotification challengeNotification = ChallengeNotification.builder()
            .message(message.toString()).challengeUser(challengeUser).build();
        challengeNotificationRepository.save(challengeNotification);
        log.info("push message={}", message);
    }
}
