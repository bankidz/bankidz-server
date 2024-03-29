package com.ceos.bankids.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
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
public class AppleKeyListDTO {

    @JsonProperty("keys")
    List<AppleKeyDTO> keys;

    public Optional<AppleKeyDTO> getMatchedKeyBy(String kid, String alg) {
        return this.keys.stream()
            .filter(key -> key.getKid().equals(kid) && key.getAlg().equals(alg))
            .findFirst();
    }
}
