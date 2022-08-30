package com.ceos.bankids.controller.request;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AllSendNotificationRequest {

    @ApiModelProperty(value = "알림 제목", example = "공지사항")
    @NotBlank(message = "알림의 제목은 필수값입니다.")
    private String title;

    @ApiModelProperty(value = "알림 내용", example = "새로운 기능 추가")
    @NotBlank(message = "알림의 내용은 필수값입니다.")
    private String body;

}