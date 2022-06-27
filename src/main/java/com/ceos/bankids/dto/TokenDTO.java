package com.ceos.bankids.dto;

import com.ceos.bankids.domain.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class TokenDTO {

    private Long id;
    private String username;
    private Boolean isKid;

    public TokenDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.isKid = user.getIsKid();
    }
}
