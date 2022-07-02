package com.ceos.bankids.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
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
public class KakaoUserDTO {

    @JsonProperty("id")
    private String authenticationCode;

    @JsonProperty("connected_at")
    private Timestamp connectedAt;

    @JsonProperty("kakao_account")
    private KakaoAccountDTO kakaoAccount;

}
