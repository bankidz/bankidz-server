package com.ceos.bankids.dto;

import com.ceos.bankids.domain.Kid;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class KidDTO {

    @ApiModelProperty(example = "8")
    Long achievedChallenge;
    @ApiModelProperty(example = "10")
    Long totalChallenge;
    @ApiModelProperty(example = "1")
    Long level;

    public KidDTO(Kid kid) {
        this.level = kid.getLevel();
    }
}
