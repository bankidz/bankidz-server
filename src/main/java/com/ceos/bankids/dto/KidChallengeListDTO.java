package com.ceos.bankids.dto;

import com.ceos.bankids.domain.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ApiModel(value = "자녀의 돈길 리스트 정보 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
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
