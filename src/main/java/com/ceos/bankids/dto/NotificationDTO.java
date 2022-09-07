package com.ceos.bankids.dto;

import com.ceos.bankids.domain.Notification;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.sql.Timestamp;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ApiModel(value = "알림 줄 때 DTO")
@Getter
@ToString
@EqualsAndHashCode
public class NotificationDTO {

    @ApiModelProperty(example = "1")
    private Long id;

    @ApiModelProperty(example = "알림 제목")
    private String title;

    @ApiModelProperty(example = "알림 내용")
    private String message;

    @ApiModelProperty(example = "false")
    private Boolean isRead;

    @ApiModelProperty(example = "2022/07/05 05:05:05")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd hh:mm:ss", timezone = "Asia/Seoul")
    private Timestamp createdAt;

    public NotificationDTO(Notification notification) {
        this.id = notification.getId();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        this.isRead = notification.getIsRead();
        this.createdAt = notification.getCreatedAt();
    }
}
