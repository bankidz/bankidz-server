package com.ceos.bankids.dto;

import com.ceos.bankids.domain.User;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KidChallengeListDTO {

    @ApiModelProperty(example = "test")
    private String userName;

    @ApiModelProperty(example = "true")
    private Boolean isFemale;

    @ApiModelProperty(example = "true")
    private List<ChallengeDTO> challengeList;

    public KidChallengeListDTO(User user, List<ChallengeDTO> challengeList) {
        this.userName = user.getUsername();
        this.isFemale = user.getIsFemale();
        this.challengeList = challengeList;
    }
}
