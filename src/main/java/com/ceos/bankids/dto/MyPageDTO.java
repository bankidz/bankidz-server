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
    UserDTO userDTO;
    @ApiModelProperty(example = "KidDTO")
    KidDTO kidDTO;
    @ApiModelProperty(example = "ParentDTO")
    ParentDTO parentDTO;

    public MyPageDTO(UserDTO userDTO, KidDTO kidDTO) {
        this.userDTO = userDTO;
        this.kidDTO = kidDTO;
    }

    public MyPageDTO(UserDTO userDTO, ParentDTO parentDTO) {
        this.userDTO = userDTO;
        this.parentDTO = parentDTO;
    }
}
