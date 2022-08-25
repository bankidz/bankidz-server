package com.ceos.bankids.dto.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
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
public class AppleKeyDTO {

    @NotNull(message = "keys may not be null")
    @JsonProperty("keys")
    List<ApplePublicKeyDTO> keys;
}
