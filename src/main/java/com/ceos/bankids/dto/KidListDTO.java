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
    private Long kidId;
    @ApiModelProperty(example = "주어랑")
    private String username;
    @ApiModelProperty(example = "true")
    private Boolean isFemale;
    @ApiModelProperty(example = "1")
    private Long level;
    @ApiModelProperty(example = "200000")
    private Long savings;
    @ApiModelProperty(example = "8")
    private Long achievedChallenge;
    @ApiModelProperty(example = "10")
    private Long totalChallenge;

    public KidListDTO(User user) {
        this.kidId = user.getKid().getId();
        this.username = user.getUsername();
        this.isFemale = user.getIsFemale();
        this.level = user.getKid().getLevel();
        this.savings = user.getKid().getSavings();
        this.achievedChallenge = user.getKid().getAchievedChallenge();
        this.totalChallenge = user.getKid().getTotalChallenge();
    }
}
