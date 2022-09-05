package com.ceos.bankids.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(value = "자녀의 완주한 돈길 리스트 DTO")
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
