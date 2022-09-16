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

    public AchievedChallengeListDTO(List<AchievedChallengeDTO> challengeList) {
        final Long[] i = {0L};
        challengeList.stream().forEach(challenge -> {
            i[0] = i[0] + challenge.getInterestPrice();
        });
        this.totalInterestPrice = i[0];
        this.challengeDTOList = challengeList;
    }
}
