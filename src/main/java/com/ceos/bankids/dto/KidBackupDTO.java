package com.ceos.bankids.dto;

import com.ceos.bankids.domain.KidBackup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class KidBackupDTO {

    private Long id;
    private String birthYear;
    private Boolean isKid;
    private Long savings;
    private Long achievedChallenge;
    private Long totalChallenge;
    private Long level;

    public KidBackupDTO(KidBackup kid) {
        this.id = kid.getId();
        this.birthYear = kid.getBirthYear();
        this.isKid = kid.getIsKid();
        this.savings = kid.getSavings();
        this.achievedChallenge = kid.getAchievedChallenge();
        this.totalChallenge = kid.getTotalChallenge();
        this.level = kid.getLevel();
    }
}
