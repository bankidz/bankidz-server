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
    @ApiModelProperty(example = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ~~~~")
    private String accessToken;

    public LoginDTO(Boolean isKid, String accessToken) {
        this.isKid = isKid;
        this.accessToken = accessToken;
    }
}
