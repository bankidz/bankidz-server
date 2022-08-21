package com.ceos.bankids.dto;

import com.google.firebase.messaging.Message;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ApiModel(value = "푸시 알림 메시지 DTO")
@Getter
@ToString
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class FcmMessageDTO {

    private boolean validate_only;
    private Message message;

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
