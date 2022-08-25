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
public class AppleRequest {

    @ApiModelProperty(example = "c95c160189da640468789aab85430d500.0.srtws.RHhe7F6wLB6e3pgu3RO1Yw")
    @NotNull(message = "code may not be null")
    private String code;

}
