package com.ceos.bankids.dto;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class FamilyDTO {

    @ApiModelProperty(example = "asdfas")
    private String code;
    @ApiModelProperty(example = "true")
    private List<FamilyUserDTO> familyUserList;


    public FamilyDTO(String code, List<FamilyUserDTO> familyUserList) {
        this.code = code;
        this.familyUserList = familyUserList;
    }
}
