package com.ceos.bankids.dto;

import com.ceos.bankids.domain.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class UserDTO {

    String username;
    Boolean isFemale;
    Boolean isKid;
    String birthday;
    String phone;


    public UserDTO(User user) {
        this.username = user.getUsername();
        this.isFemale = user.getIsFemale();
        this.isKid = user.getIsKid();
        this.birthday = user.getBirthday();
        this.phone = user.getPhone();
    }
}
