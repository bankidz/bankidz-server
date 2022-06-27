package com.ceos.bankids.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoRequest {

    @NotNull(message = "accessToken may not be null")
    @JsonProperty("access_token")
    private String accessToken;

    @NotNull(message = "isKid may not be null")
    private Boolean isKid;

    private String period;

    private Long allowance;
}
