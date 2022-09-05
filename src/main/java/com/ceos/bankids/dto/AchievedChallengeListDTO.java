package com.ceos.bankids.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(value = "완주한 돈길 리스트 DTO")
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class AchievedChallengeListDTO {

    @ApiModelProperty(example = "5000")
    private Long totalInterestPrice;

    @ApiModelProperty(example = "true")
    private List<AchievedChallengeDTO> challengeDTOList;

    public AchievedChallengeListDTO(Long interestPrice, List<AchievedChallengeDTO> challengeList) {
        this.totalInterestPrice = interestPrice;
        this.challengeDTOList = challengeList;
    }
}
