package com.ceos.bankids.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class ChallengeCompleteDeleteByKidMapperDTO {

    private Long momTotalRequest;

    private Long momAcceptedRequest;

    private Long dadTotalRequest;

    private Long dadAcceptedRequest;

    public ChallengeCompleteDeleteByKidMapperDTO(Long momTotalRequest, Long momAcceptedRequest,
        Long dadTotalRequest, Long dadAcceptedRequest) {
        this.momTotalRequest = momTotalRequest;
        this.momAcceptedRequest = momAcceptedRequest;
        this.dadTotalRequest = dadTotalRequest;
        this.dadAcceptedRequest = dadAcceptedRequest;
    }
}
