package com.ceos.bankids.controller.request;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KidChallengeRequest {

    @NotNull(message = "돈길 수락 여부를 입력해주세요")
    private Boolean accept;

    private String comment;
}
