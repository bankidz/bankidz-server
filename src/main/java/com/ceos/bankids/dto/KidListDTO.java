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
    @ApiModelProperty(example = "주어랑")
    String username;
    @ApiModelProperty(example = "true")
    Boolean isFemale;
    @ApiModelProperty(example = "1")
    Long level;

    public KidListDTO(User user) {
        this.kidId = user.getKid().getId();
        this.username = user.getUsername();
        this.isFemale = user.getIsFemale();
        this.level = user.getKid().getLevel();
    }
}
