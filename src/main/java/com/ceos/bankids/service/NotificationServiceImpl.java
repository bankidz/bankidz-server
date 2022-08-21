package com.ceos.bankids.service;

import com.ceos.bankids.constant.ErrorCode;
import com.ceos.bankids.dto.FcmMessageDTO;
import com.ceos.bankids.exception.InternalServerException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import java.io.IOException;
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
    public String makeChallengeStatusMessage(FcmMessageDTO fcmMessageDTO)
        throws JsonProcessingException {

//        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(new FileInputStream(FCM_PRIVATE_KEY_PATH)).createScoped()
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("content-type", MediaType.APPLICATION_JSON_VALUE);
//        headers.add("Authorization", "Bearer " + googleCredential.getAccessToken());

        try {
            FirebaseMessaging.getInstance().send(fcmMessageDTO.getMessage());
        } catch (FirebaseMessagingException e) {
            log.error("service err={}", e);
            throw new InternalServerException(ErrorCode.NOTIFICATION_SERVICE_ERROR.getErrorCode());
        }

        log.info("push message={}", objectMapper.writeValueAsString(fcmMessageDTO));
        return objectMapper.writeValueAsString(fcmMessageDTO);
    }

}
