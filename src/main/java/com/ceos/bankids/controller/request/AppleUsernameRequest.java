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
public class AppleUsernameRequest {

    @ApiModelProperty(example = "키즈")
    @NotNull(message = "firstName may not be null")
    private String firstName;
    
    @ApiModelProperty(example = "뱅")
    @NotNull(message = "lastName may not be null")
    private String lastName;

}
