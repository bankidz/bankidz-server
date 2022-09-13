package com.ceos.bankids.dto;

import com.ceos.bankids.controller.request.ChallengeRequest;
import com.ceos.bankids.domain.User;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChallengePostDTO {

    @ApiModelProperty(example = "false")
    @NotNull(message = "계약 대상의 성별은 필수값입니다.")
    private User contractUser;

    @ApiModelProperty(example = "이자율 받기")
    @NotBlank(message = "돈길의 카테고리를 입력해주세요")
    private String challengeCategory;

    @ApiModelProperty(example = "전자제품")
    @NotBlank(message = "돈길의 목표 아이템을 입력해주세요")
    private String itemName;

    @ApiModelProperty(example = "에어팟 사기")
    @NotBlank(message = "돈길의 제목을 입력해주세요")
    private String title;

    @ApiModelProperty(example = "30")
    @NotNull(message = "돈길의 이자율을 입력해주세요")
    private Long interestRate;

    @ApiModelProperty(example = "3000")
    @NotNull(message = "돈길의 총 이자 금액을 입력해주세요")
    private Long interestPrice;

    @ApiModelProperty(example = "150000")
    @NotNull(message = "돈길의 목표 금액을 입력해주세요")
    private Long totalPrice;


    @ApiModelProperty(example = "10000")
    @NotNull(message = "돈길의 주당 금액을 입력해주세요")
    private Long weekPrice;

    @ApiModelProperty(example = "15")
    @NotNull(message = "돈길의 총 주차를 입력해주세요")
    private Long weeks;

    @ApiModelProperty(example = "fileName")
    @NotNull(message = "사인 이미지 이름을 입력해주세요")
    private String fileName;

    public ChallengePostDTO(ChallengeRequest challengeRequest, User contractUser) {
        this.contractUser = contractUser;
        this.title = challengeRequest.getTitle();
        this.challengeCategory = challengeRequest.getChallengeCategory();
        this.fileName = challengeRequest.getFileName();
        this.interestPrice = challengeRequest.getInterestPrice();
        this.interestRate = challengeRequest.getInterestRate();
        this.itemName = challengeRequest.getItemName();
        this.totalPrice = challengeRequest.getTotalPrice();
        this.weekPrice = challengeRequest.getWeekPrice();
        this.weeks = challengeRequest.getWeeks();
    }
}
