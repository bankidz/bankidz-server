package com.ceos.bankids.dto;

import com.ceos.bankids.constant.ChallengeStatus;
import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.Progress;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import java.sql.Timestamp;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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

    @ApiModelProperty(example = "ACHIEVED")
    private ChallengeStatus challengeStatus;

    @ApiModelProperty(example = "2022/05/05 06:33:13")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp approvedAt;


    public ProgressDTO(Progress progress, Challenge challenge) {
        this.challengeId = progress.getChallenge().getId();
        this.weeks = progress.getWeeks();
        this.isAchieved = progress.getIsAchieved();
        this.challengeStatus = challenge.getChallengeStatus();
        this.approvedAt = progress.getCreatedAt();
    }
}
