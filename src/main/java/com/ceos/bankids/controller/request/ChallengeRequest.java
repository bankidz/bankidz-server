package com.ceos.bankids.controller.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeRequest {

    @ApiModelProperty(example = "이자율 받기")
    @NotBlank(message = "돈길의 카테고리를 입력해주세요")
    private String category;

    @ApiModelProperty(example = "전자제품")
    @NotBlank(message = "돈길의 목표 아이템을 입력해주세요")
    private String itemName;

    @ApiModelProperty(example = "에어팟 사기")
    @NotBlank(message = "돈길의 제목을 입력해주세요")
    private String title;

    @ApiModelProperty(example = "30")
    @NotNull(message = "돈길의 이자율을 입력해주세요")
    private Long interestRate;

    @ApiModelProperty(example = "150000")
    @NotNull(message = "돈길의 목표 금액을 입력해주세요")
    private Long totalPrice;

    @ApiModelProperty(example = "3000")
    @NotNull(message = "돈길의 주당 금액을 입력해주세요")
    private Long weekPrice;

    @ApiModelProperty(example = "15")
    @NotNull(message = "돈길의 총 주차를 입력해주세요")
    @Min(value = 3, message = "주차수가 너무 적습니다.")
    @Max(value = 15, message = "주차수가 너무 많습니다.")
    private Long weeks;

}
