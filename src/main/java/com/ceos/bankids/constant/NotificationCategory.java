package com.ceos.bankids.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationCategory implements EnumMapperType {

    CHALLENGE,
    LEVEL,
    NOTICE,
    FAMILY;

    @Override
    public String getCode() {
        return name();
    }
}
