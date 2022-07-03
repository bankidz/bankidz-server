package com.ceos.bankids.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class LoginDTO {

    private Boolean isRegistered;
    private Boolean isKid;
    private String accessToken;

    public LoginDTO(Boolean isRegistered, Boolean isKid, String accessToken) {
        this.isRegistered = isRegistered;
        this.isKid = isKid;
        this.accessToken = accessToken;
    }
}
