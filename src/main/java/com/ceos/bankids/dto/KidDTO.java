package com.ceos.bankids.dto;

import com.ceos.bankids.domain.Kid;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class KidDTO {
    String username;
    String image;

    public KidDTO(Kid kid) {
        this.username = kid.getUsername();
        this.image = kid.getImage();
    }
}
