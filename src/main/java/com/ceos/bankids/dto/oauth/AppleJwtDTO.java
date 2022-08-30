package com.ceos.bankids.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AppleJwtDTO {

    @JsonProperty("iss")
    private String iss;

    @JsonProperty("aud")
    private String aud;

    @JsonProperty("exp")
    private Long exp;

    @JsonProperty("iat")
    private Long iat;

    @JsonProperty("sub")
    private String sub;

    @JsonProperty("nonce")
    private String nonce;

    @JsonProperty("nonce_supported")
    private Boolean nonceSupported;
    
    @JsonProperty("c_hash")
    private String cHash;

    @JsonProperty("email")
    private String email;

    @JsonProperty("email_verified")
    private String emailVerified;

    @JsonProperty("is_private_email")
    private String isPrivateEmail;

    @JsonProperty("auth_time")
    private Long authTime;

}
