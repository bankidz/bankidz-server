package com.ceos.bankids.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ApiModel(value = "읽지 않은 알림 확인 DTO")
@Getter
@ToString
@EqualsAndHashCode
public class NotificationIsReadDTO {

    @ApiModelProperty(example = "true")
    private Boolean isAllRead;

    public NotificationIsReadDTO(Boolean isAllRead) {
        this.isAllRead = isAllRead;
    }
}
