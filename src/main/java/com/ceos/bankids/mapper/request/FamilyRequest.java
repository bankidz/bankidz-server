package com.ceos.bankids.mapper.request;

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
public class FamilyRequest {

    @ApiModelProperty(example = "863035e8-b067-4dae-8961-b52e6839a81b")
    @NotNull(message = "code may not be null")
    private String code;
}
