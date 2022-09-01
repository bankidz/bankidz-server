package com.ceos.bankids.dto;

import com.ceos.bankids.domain.ParentBackup;
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
public class ParentBackupDTO {

    private Long id;
    private String birthYear;
    private Boolean isKid;
    private Long acceptedRequest;
    private Long totalRequest;

    public ParentBackupDTO(ParentBackup parent) {
        this.id = parent.getId();
        this.birthYear = parent.getBirthYear();
        this.isKid = parent.getIsKid();
        this.acceptedRequest = parent.getAcceptedRequest();
        this.totalRequest = parent.getTotalRequest();
    }
}
