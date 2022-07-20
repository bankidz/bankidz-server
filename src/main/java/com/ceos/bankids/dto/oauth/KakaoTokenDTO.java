package com.ceos.bankids.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
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
public class KakaoTokenDTO {

    @NotNull(message = "accessToken may not be null")
    @JsonProperty("access_token")
    private String accessToken;

    @NotNull(message = "refreshToken may not be null")
    @JsonProperty("refresh_token")
    private String refreshToken;
    
}
