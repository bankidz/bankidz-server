package com.ceos.bankids.dto;

import com.ceos.bankids.domain.Kid;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class KidWeekDTO {

    @ApiModelProperty(example = "")
    private Long kidId;

    @ApiModelProperty
    private WeekDTO weekInfo;

    public KidWeekDTO(Kid kid, WeekDTO weekDTO) {
        this.kidId = kid.getId();
        this.weekInfo = weekDTO;
    }
}
