package com.ceos.bankids.controller.request;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(example = "19990521")
    @NotNull(message = "birthday may not be null")
    private String birthday;

    @ApiModelProperty(example = "false")
    @NotNull(message = "isFemale may not be null")
    private Boolean isFemale;

    @ApiModelProperty(example = "true")
    @NotNull(message = "isKid may not be null")
    private Boolean isKid;
}
