package com.ceos.bankids.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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
