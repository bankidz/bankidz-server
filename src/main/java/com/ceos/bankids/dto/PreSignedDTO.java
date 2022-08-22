package com.ceos.bankids.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.net.URL;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(value = "preSignedUrl 받아오는 DTO")
@Setter
@Getter
@ToString
@EqualsAndHashCode
public class PreSignedDTO {

    @ApiModelProperty(value = "url")
    private URL preSignedUrl;

    @ApiModelProperty(value = "url")
    private String imageName;

    public PreSignedDTO(URL preSignedUrl, String imageName) {
        this.preSignedUrl = preSignedUrl;
        this.imageName = imageName;
    }
}
