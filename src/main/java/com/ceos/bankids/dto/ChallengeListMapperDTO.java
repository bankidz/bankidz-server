package com.ceos.bankids.dto;

import com.ceos.bankids.domain.Challenge;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class ChallengeListMapperDTO {

    private Challenge challenge;

    private List<ProgressDTO> progressDTOList;

    private Boolean changeStatus;

    public ChallengeListMapperDTO(Challenge challenge, List<ProgressDTO> progressDTOList,
        Boolean changeStatus) {
        this.challenge = challenge;
        this.progressDTOList = progressDTOList;
        this.changeStatus = changeStatus;
    }
}
