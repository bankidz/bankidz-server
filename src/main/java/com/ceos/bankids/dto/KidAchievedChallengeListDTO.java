package com.ceos.bankids.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class KidAchievedChallengeListDTO {

    @ApiModelProperty(example = "1")
    private Long kidId;

    @ApiModelProperty(example = "true")
    private AchievedChallengeListDTO achievedChallengeListDTO;

    public KidAchievedChallengeListDTO(Long kidId,
        AchievedChallengeListDTO achievedChallengeListDTO) {
        this.kidId = kidId;
        this.achievedChallengeListDTO = achievedChallengeListDTO;
    }
}
