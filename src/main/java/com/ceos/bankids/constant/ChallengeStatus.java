package com.ceos.bankids.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChallengeStatus implements EnumMapperType {

    REJECTED(1L, 0L, "부모가 거절한 상태", "rejected"),
    ACHIEVED(2L, 0L, "돈길을 완전히 완주한 상태", "achieved"),
    FAILED(0L, 0L, "이자율 위험도에 걸려서 실패한 상태", "failed"),
    PENDING(1L, 1L, "부모한테 제안한 상태", "pending"),
    WALKING(1L, 2L, "부모한테 수락 받아서 걷고있는 상태", "walking");

    private final Long isAchieved;
    private final Long status;
    private final String description;
    private final String statusName;

    @Override
    public String getCode() {
        return name();
    }
}
