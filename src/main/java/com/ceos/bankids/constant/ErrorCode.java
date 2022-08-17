package com.ceos.bankids.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode implements EnumMapperType {

    // Kakao
    KAKAO_BAD_REQUEST("E401-10001"),

    // User

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
