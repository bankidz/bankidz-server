package com.ceos.bankids.dto;

import com.ceos.bankids.domain.User;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class KidChallengeListDTO {

    @ApiModelProperty(example = "1")
    private Long kidId;

    @ApiModelProperty(example = "true")
    private List<ChallengeDTO> challengeList;

    public KidChallengeListDTO(User user, List<ChallengeDTO> challengeList) {
        this.kidId = user.getKid().getId();
        this.challengeList = challengeList;
    }
}
