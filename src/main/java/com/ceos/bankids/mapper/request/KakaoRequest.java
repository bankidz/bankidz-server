package com.ceos.bankids.mapper.request;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoRequest {

    @ApiModelProperty(example = "erYw3skzA27NEcbri4hIu18MZ7PRKzhsvQB31JU-iyq1BWgzYv1xve8KpjV_24CwGx3PiwopyNgAAAGBxHM1jQ")
    @NotNull(message = "code may not be null")
    private String code;
}
