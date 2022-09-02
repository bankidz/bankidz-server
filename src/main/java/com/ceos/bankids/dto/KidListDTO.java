package com.ceos.bankids.dto;

import com.ceos.bankids.domain.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class KidListDTO {

    @ApiModelProperty(example = "1")
    Long kidId;
    @ApiModelProperty(example = "true")
    Boolean isFemale;
    @ApiModelProperty(example = "1")
    Long level;
    @ApiModelProperty(example = "200000")
    Long savings;
    @ApiModelProperty(example = "8")
    Long achievedChallenge;
    @ApiModelProperty(example = "10")
    Long totalChallenge;

    public KidListDTO(User user) {
        this.kidId = user.getKid().getId();
        this.isFemale = user.getIsFemale();
        this.level = user.getKid().getLevel();
        this.savings = user.getKid().getSavings();
        this.achievedChallenge = user.getKid().getAchievedChallenge();
        this.totalChallenge = user.getKid().getTotalChallenge();
    }
}
