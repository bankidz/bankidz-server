package com.ceos.bankids.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class LoginDTO {

    @ApiModelProperty(example = "true")
    private Boolean isKid;
    @ApiModelProperty(example = "1")
    private Long level;
    @ApiModelProperty(example = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ~~~~")
    private String accessToken;
    @ApiModelProperty(example = "kakao")
    private String provider;

    public LoginDTO(Boolean isKid, String accessToken) {
        this.isKid = isKid;
        this.accessToken = accessToken;
    }

    public LoginDTO(Boolean isKid, String accessToken, Long level) {
        this.isKid = isKid;
        this.level = level;
        this.accessToken = accessToken;
    }
}
