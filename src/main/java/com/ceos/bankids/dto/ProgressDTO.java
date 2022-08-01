package com.ceos.bankids.dto;

import com.ceos.bankids.domain.Progress;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ApiModel(value = "progress 정보 DTO")
@Getter
@ToString
@EqualsAndHashCode
public class ProgressDTO {

    @ApiModelProperty(example = "2")
    private Long challengeId;

    @ApiModelProperty(example = "1")
    private Long weeks;

    @ApiModelProperty(example = "true")
    private Boolean isAchieved;

    public ProgressDTO(Progress progress) {
        this.challengeId = progress.getChallenge().getId();
        this.weeks = progress.getWeeks();
        this.isAchieved = progress.getIsAchieved();
    }
}
