package com.ceos.bankids.service;

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
}
