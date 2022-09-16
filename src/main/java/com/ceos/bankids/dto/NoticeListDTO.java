package com.ceos.bankids.dto;

import com.ceos.bankids.domain.Notice;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import java.sql.Timestamp;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class NoticeListDTO {

    @ApiModelProperty(example = "1")
    private Long id;

    @ApiModelProperty(example = "공지사항")
    private String title;

    @ApiModelProperty(example = "2022/07/05 05:05:05")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd kk:mm:ss", timezone = "Asia/Seoul")
    private Timestamp createdAt;

    public NoticeListDTO(Notice notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.createdAt = notice.getCreatedAt();
    }
}
