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
public class WithdrawalRequest {

    @ApiModelProperty(example = "나 탈퇴하겠어!")
    @NotNull(message = "message may not be null")
    private String message;

}
