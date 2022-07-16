package com.ceos.bankids.dto;

import com.ceos.bankids.domain.Parent;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class ParentDTO {

    @ApiModelProperty(example = "15")
    Long totalChallenge;
    @ApiModelProperty(example = "8")
    Long acceptedRequest;
    @ApiModelProperty(example = "10")
    Long totalRequest;
    @ApiModelProperty(example = "300000")
    Long savings;

    public ParentDTO(Parent parent) {
        this.totalChallenge = parent.getTotalChallenge();
        this.acceptedRequest = parent.getAcceptedRequest();
        this.totalRequest = parent.getTotalRequest();
        this.savings = parent.getSavings();
    }
}
