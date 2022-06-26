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
    String image;

    public UserDTO(User user) {
        this.username = user.getUsername();
        this.image = user.getImage();
    }
}
