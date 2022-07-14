package com.ceos.bankids.dto;

import com.ceos.bankids.domain.Challenge;
import com.ceos.bankids.domain.Comment;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import java.sql.Timestamp;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
@EqualsAndHashCode
public class ChallengeDTO {

    @ApiModelProperty(example = "1")
    private Long id;

    @ApiModelProperty(example = "true")
    private Boolean isMom;

    @ApiModelProperty(example = "에어팟 사기")
    private String title;

    private String targetItemName;

    private String challengeCategoryName;

    @ApiModelProperty(example = "false")
    private Boolean isAchieved;

    @ApiModelProperty(example = "30")
    private Long interestRate;

    @ApiModelProperty(example = "150000")
    private Long totalPrice;

    @ApiModelProperty(example = "10000")
    private Long weekPrice;

    @ApiModelProperty(example = "15")
    private Long weeks;

    @ApiModelProperty(example = "2022-07-05 05:05:05")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss", timezone = "Asia/Seoul")
    private Timestamp createdAt;

    @ApiModelProperty(example = "1")
    private Long status;

    @ApiModelProperty(example = "true")
    private List<ProgressDTO> progressList;

    private Comment comment;

    public ChallengeDTO(Challenge challenge, List<ProgressDTO> progressDTOList, Comment comment) {
        this.id = challenge.getId();
        this.isMom = challenge.getContractUser().getIsFemale();
        this.title = challenge.getTitle();
        this.targetItemName = challenge.getTargetItem().getName();
        this.challengeCategoryName = challenge.getChallengeCategory().getCategory();
        this.isAchieved = challenge.getIsAchieved();
        this.interestRate = challenge.getInterestRate();
        this.totalPrice = challenge.getTotalPrice();
        this.weekPrice = challenge.getWeekPrice();
        this.weeks = challenge.getWeeks();
        this.createdAt = challenge.getCreatedAt();
        this.status = challenge.getStatus();
        this.progressList = progressDTOList;
        this.comment = comment;
    }
}
