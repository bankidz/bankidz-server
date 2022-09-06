package com.ceos.bankids.dto;

import com.ceos.bankids.domain.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class OptInDTO {

    @ApiModelProperty(example = "false")
    private Boolean noticeOptIn;
    @ApiModelProperty(example = "true")
    private Boolean actionOptIn;

    public OptInDTO(User user) {
        this.noticeOptIn = user.getNoticeOptIn();
        this.actionOptIn = user.getActionOptIn();
    }
}
