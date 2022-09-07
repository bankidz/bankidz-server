package com.ceos.bankids.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ApiModel(value = "알림 리스트로 줄 때 DTO")
@Getter
@ToString
@EqualsAndHashCode
public class NotificationListDTO {

    @ApiModelProperty(example = "6")
    private Long lastId;

    @ApiModelProperty(example = "list")
    private List<NotificationDTO> notificationList;

    @ApiModelProperty(example = "false")
    private Boolean isLast;

    public NotificationListDTO(Long lastId, Boolean isLast,
        List<NotificationDTO> notificationList) {
        this.lastId = lastId;
        this.notificationList = notificationList;
        this.isLast = isLast;
    }
}
