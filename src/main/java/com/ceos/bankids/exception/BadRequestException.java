package com.ceos.bankids.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {

    public BadRequestException() {
        super(HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
