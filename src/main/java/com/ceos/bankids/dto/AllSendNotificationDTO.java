package com.ceos.bankids.dto;

import io.swagger.annotations.ApiModel;
import java.util.HashMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ApiModel(value = "allSendNotification 내부 DTO")
@Getter
@ToString
@EqualsAndHashCode
public class AllSendNotificationDTO {

    private String title;

    private String message;

    private HashMap<String, Object> newMap;

    public AllSendNotificationDTO(String title, String message, HashMap<String, Object> newMap) {
        this.title = title;
        this.message = message;
        this.newMap = newMap;
    }
}
