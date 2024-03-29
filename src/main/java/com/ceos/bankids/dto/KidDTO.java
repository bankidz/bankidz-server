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
    private Long achievedChallenge;
    @ApiModelProperty(example = "10")
    private Long totalChallenge;
    @ApiModelProperty(example = "1")
    private Long level;

    public KidDTO(Kid kid) {
        this.achievedChallenge = kid.getAchievedChallenge();
        this.totalChallenge = kid.getTotalChallenge();
        this.level = kid.getLevel();
    }
}
