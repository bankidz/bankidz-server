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
public class ApplePublicKeyDTO {

    @NotNull(message = "kty may not be null")
    @JsonProperty("kty")
    private String kty;

    @NotNull(message = "kid may not be null")
    @JsonProperty("kid")
    private String kid;

    @NotNull(message = "use may not be null")
    @JsonProperty("use")
    private String use;

    @NotNull(message = "alg may not be null")
    @JsonProperty("alg")
    private String alg;

    @NotNull(message = "n may not be null")
    @JsonProperty("n")
    private String n;

    @NotNull(message = "e may not be null")
    @JsonProperty("e")
    private String e;
}
