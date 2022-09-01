package com.ceos.bankids.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class MyPageDTO {

    @ApiModelProperty(example = "UserDTO")
    private UserDTO user;
    @ApiModelProperty(example = "KidDTO")
    private KidDTO kid;
    @ApiModelProperty(example = "ParentDTO")
    private ParentDTO parent;

    public MyPageDTO(UserDTO userDTO, KidDTO kidDTO) {
        this.user = userDTO;
        this.kid = kidDTO;
    }

    public MyPageDTO(UserDTO userDTO, ParentDTO parentDTO) {
        this.user = userDTO;
        this.parent = parentDTO;
    }
}
