package com.ceos.bankids.dto;

import com.ceos.bankids.constant.ChallengeStatus;
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

    @ApiModelProperty(example = "전자제품")
    private String itemName;

    @ApiModelProperty(example = "PENDING")
    private ChallengeStatus challengeStatus;

    @ApiModelProperty(example = "부모와 함께 하기")
    private String challengeCategory;

    @ApiModelProperty(example = "30")
    private Long interestRate;

    @ApiModelProperty(example = "3000")
    private Long interestPrice;

    @ApiModelProperty(example = "150000")
    private Long totalPrice;

    @ApiModelProperty(example = "10000")
    private Long weekPrice;

    //succeededWeeks
    @ApiModelProperty(example = "0")
    private Long successWeeks;

    @ApiModelProperty(example = "15")
    private Long weeks;

    @ApiModelProperty(example = "2022/07/05 05:05:05")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp createdAt;

    @ApiModelProperty(example = "true")
    private List<ProgressDTO> progressList;

    private Comment comment;

    private String fileName;

    public ChallengeDTO(Challenge challenge, List<ProgressDTO> progressDTOList, Comment comment) {
        this.id = challenge.getId();
        this.isMom = challenge.getContractUser().getIsFemale();
        this.title = challenge.getTitle();
        this.challengeStatus = challenge.getChallengeStatus();
        this.itemName = challenge.getTargetItem().getName();
        this.challengeCategory = challenge.getChallengeCategory().getCategory();
        this.interestRate = challenge.getInterestRate();
        this.interestPrice = challenge.getInterestPrice();
        this.totalPrice = challenge.getTotalPrice();
        this.weekPrice = challenge.getWeekPrice();
        this.successWeeks = challenge.getSuccessWeeks();
        this.weeks = challenge.getWeeks();
        this.createdAt = challenge.getCreatedAt();
        this.progressList = progressDTOList;
        this.comment = comment;
        this.fileName = challenge.getFileName();
    }
}
