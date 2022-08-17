package com.ceos.bankids.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode implements EnumMapperType {

    // Kakao

    // User

    // Family
    MOM_ALREADY_EXISTS("E403-30001"); // example

    // Challenge

    // Progress

    // S3

    private final String errorCode;

    @Override
    public String getCode() {
        return name();
    }
}
