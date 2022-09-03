package com.ceos.bankids.dto;

import com.ceos.bankids.domain.Family;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class FamilyDTO {

    @ApiModelProperty(example = "1")
    private Long id;
    @ApiModelProperty(example = "asdfas")
    private String code;
    @ApiModelProperty(example = "[]")
    private List<FamilyUserDTO> familyUserList;


    @Builder
    public FamilyDTO(Family family, List<FamilyUserDTO> familyUserList) {
        this.id = family.getId();
        this.code = family.getCode();
        this.familyUserList = familyUserList;
    }

}
