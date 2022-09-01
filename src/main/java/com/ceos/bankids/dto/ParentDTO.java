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

    @ApiModelProperty(example = "8")
    private Long acceptedRequest;
    @ApiModelProperty(example = "10")
    private Long totalRequest;

    public ParentDTO(Parent parent) {
        this.acceptedRequest = parent.getAcceptedRequest();
        this.totalRequest = parent.getTotalRequest();
    }
}
