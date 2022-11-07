package com.ceos.bankids.dto;

import com.ceos.bankids.domain.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import java.sql.Timestamp;
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
    private Boolean serviceOptIn;
    @ApiModelProperty(example = "2022/07/05 05:05:05")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp updatedAt;

    public OptInDTO(User user) {
        this.noticeOptIn = user.getNoticeOptIn();
        this.serviceOptIn = user.getServiceOptIn();
        this.updatedAt = user.getUpdatedAt();
    }
}
