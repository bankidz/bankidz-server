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
public class AppleKeyDTO {

    @JsonProperty("kty")
    private String kty;

    @JsonProperty("kid")
    private String kid;

    @JsonProperty("use")
    private String use;

    @JsonProperty("alg")
    private String alg;

    @JsonProperty("n")
    private String n;

    @JsonProperty("e")
    private String e;

}
