package com.ceos.bankids.dto;

import com.ceos.bankids.domain.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class UserDTO {

    @ApiModelProperty(example = "주어랑")
    String username;
    @ApiModelProperty(example = "true")
    Boolean isFemale;
    @ApiModelProperty(example = "true")
    Boolean isKid;
    @ApiModelProperty(example = "19990521")
    String birthday;
    @ApiModelProperty(example = "01019990521")
    String phone;


    public UserDTO(User user) {
        this.username = user.getUsername();
        this.isFemale = user.getIsFemale();
        this.isKid = user.getIsKid();
        this.birthday = user.getBirthday();
        this.phone = user.getPhone();
    }
}
