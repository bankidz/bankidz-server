package com.ceos.bankids.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode implements EnumMapperType {

    // Kakao
    KAKAO_BAD_REQUEST("E400-10001"),

    // User
    USER_NOT_EXISTS("E400-20001"),
    USER_ALREADY_HAS_TYPE("E400-20002"),
    INVALID_BIRTHDAY("E400-20003"),
    USER_TYPE_NOT_CHOSEN("E400-20004"),

    // Family
    MOM_ALREADY_EXISTS("E403-30001"),

    // Challenge

    // Progress

    // S3

    private final String errorCode;

    @Override
    public String getCode() {
        return name();
    }
}
