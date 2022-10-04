package com.ceos.bankids.dto;

import com.google.firebase.messaging.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class FcmMessageDTO {

    private boolean validate_only;
    private Message message;
    private Long challengeId;
    private Long userId;

//    @Builder
//    @AllArgsConstructor
//    @Getter
//    public static class Message {
//
//        private Notification notification;
//        private String token;
//    }

//    @Builder
//    @AllArgsConstructor
//    @Getter
//    public static class Notification {
//
//        private String title;
//        private String body;
//        private String image;
//    }
}
