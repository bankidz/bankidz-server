package com.ceos.bankids.mapper.request;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KidChallengeRequest {

    @ApiModelProperty(example = "false")
    @NotNull(message = "돈길 수락 여부를 입력해주세요")
    private Boolean accept;

    @ApiModelProperty(example = "아쉽구나...")
    private String comment;
}
