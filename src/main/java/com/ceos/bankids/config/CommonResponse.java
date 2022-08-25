package com.ceos.bankids.config;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {

    String error;
    T data;


    public static <T> CommonResponse<T> onSuccess(T data) {
        return new CommonResponse<>(null, data);
    }

    public static CommonResponse onFailure(String errorCode) {
        return new CommonResponse<>(errorCode, null);
    }
}
