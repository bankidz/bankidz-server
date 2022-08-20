package com.ceos.bankids.service;

import com.ceos.bankids.dto.FcmMessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
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
    @Override
    public String makeChallengeStatusMessage(String token, String title, String body, String path)
        throws JsonProcessingException {
        FcmMessageDTO fcmMessageDTO = FcmMessageDTO.builder()
            .message(FcmMessageDTO.Message.builder().token("token")
                .notification(
                    FcmMessageDTO.Notification.builder().title(title).body(body).image("image")
                        .build()).build())
            .validate_only(false).build();

        log.info("push message={}", objectMapper.writeValueAsString(fcmMessageDTO));
        return objectMapper.writeValueAsString(fcmMessageDTO);
    }

}
