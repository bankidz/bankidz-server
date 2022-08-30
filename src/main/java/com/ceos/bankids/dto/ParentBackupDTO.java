package com.ceos.bankids.dto;

import com.ceos.bankids.domain.ParentBackup;

public class ParentBackupDTO {

    private String birthYear;
    private Boolean isKid;
    private Long acceptedRequest;
    private Long totalRequest;

    public ParentBackupDTO(ParentBackup parent) {
        this.birthYear = parent.getBirthYear();
        this.isKid = parent.getIsKid();
        this.acceptedRequest = parent.getAcceptedRequest();
        this.totalRequest = parent.getTotalRequest();
    }
}
