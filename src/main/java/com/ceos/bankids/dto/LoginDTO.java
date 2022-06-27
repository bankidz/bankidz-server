package com.ceos.bankids.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class LoginDTO {

    private Boolean isRegistered;
    private String accessToken;

    public LoginDTO(Boolean isRegistered, String accessToken) {
        this.isRegistered = isRegistered;
        this.accessToken = accessToken;
    }
}
