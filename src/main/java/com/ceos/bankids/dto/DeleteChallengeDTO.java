package com.ceos.bankids.dto;

import com.ceos.bankids.domain.Challenge;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class DeleteChallengeDTO {

    @ApiModelProperty(example = "1")
    private Long id;

    public DeleteChallengeDTO(Challenge challenge) {
        this.id = challenge.getId();
    }
}
