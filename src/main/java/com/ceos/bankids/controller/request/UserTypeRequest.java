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
public class UserTypeRequest {

    @NotNull(message = "isFemale may not be null")
    private Boolean isFemale;

    @NotNull(message = "isKid may not be null")
    private Boolean isKid;
}
