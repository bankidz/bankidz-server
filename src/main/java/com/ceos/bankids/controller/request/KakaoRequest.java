package com.ceos.bankids.controller.request;

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
    private String accessToken;

    @NotNull(message = "isKid may not be null")
    private Boolean isKid;

    private String period;

    private Long allowance;
}
