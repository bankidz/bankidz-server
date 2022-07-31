package com.ceos.bankids.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ApiModel(value = "주차 정보 DTO")
@Getter
@ToString
@EqualsAndHashCode
public class WeekDTO {

    @ApiModelProperty(example = "3000")
    private Long currentSavings;

    @ApiModelProperty(example = "300000")
    private Long totalPrice;

    public WeekDTO(Long currentSavings, Long totalPrice) {
        this.currentSavings = currentSavings;
        this.totalPrice = totalPrice;
    }
}
