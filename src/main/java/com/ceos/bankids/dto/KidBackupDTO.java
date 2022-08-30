package com.ceos.bankids.dto;

import com.ceos.bankids.domain.KidBackup;

public class KidBackupDTO {

    private String birthYear;
    private Boolean isKid;
    private Long savings;
    private Long achievedChallenge;
    private Long totalChallenge;
    private Long level;

    public KidBackupDTO(KidBackup kid) {
        this.birthYear = kid.getBirthYear();
        this.isKid = kid.getIsKid();
        this.savings = kid.getSavings();
        this.achievedChallenge = kid.getAchievedChallenge();
        this.totalChallenge = kid.getTotalChallenge();
        this.level = kid.getLevel();
    }
}
