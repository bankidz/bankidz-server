package com.ceos.bankids.dto;

import com.ceos.bankids.domain.Challenge;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class AchievedChallengeDTO {

    @ApiModelProperty(example = "true")
    private ChallengeDTO challenge;

    @ApiModelProperty(example = "3000")
    private Long interestPrice;

    public AchievedChallengeDTO(Challenge challenge) {
        List<ProgressDTO> progressDTOList = challenge.getProgressList().stream()
            .map(progress -> new ProgressDTO(progress, challenge))
            .collect(
                Collectors.toList());
        this.challenge = new ChallengeDTO(challenge, progressDTOList, null);
        this.interestPrice =
            (challenge.getInterestPrice() / challenge.getWeeks()) * challenge.getSuccessWeeks();

    }
}
