package com.ceos.bankids.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends BaseException {

    public InternalServerException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalServerException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

}
