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
    FAMILY_NOT_EXISTS("E403-30001"),
    KID_FORBIDDEN("E403-30002"),
    FAMILY_TO_JOIN_NOT_EXISTS("E400-30003"),
    INVALID_USER_TYPE("E400-30004"),
    MOM_ALREADY_EXISTS("E403-30005"),
    DAD_ALREADY_EXISTS("E403-30006"),
    USER_ALREADY_IN_FAMILY("E403-30007");

    // Challenge

    // Progress

    // S3

    private final String errorCode;

    @Override
    public String getCode() {
        return name();
    }
}
