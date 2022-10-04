package com.ceos.bankids.dto;

import java.util.HashMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class ChallengeCompleteDeleteByParentMapperDTO {

    private HashMap<Long, Long> kidIdMappingToKidTotalChallenge;

    private HashMap<Long, Long> kidIdMappingToKidAchievedChallenge;

    private HashMap<Long, Long> kidIdMappingToKidSavings;
}
