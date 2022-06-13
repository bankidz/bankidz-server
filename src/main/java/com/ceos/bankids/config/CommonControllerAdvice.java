package com.ceos.bankids.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.ceos.bankids.exception.BaseException;

@ControllerAdvice
public class CommonControllerAdvice {

    @ExceptionHandler(value = BaseException.class)
    public ResponseEntity onKnownException(BaseException baseException) {
        return new ResponseEntity<>(CommonResponse.onFailure(baseException.getStatusCode(),
            baseException.getResponseMessage()), null, baseException.getStatusCode());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity onException(Exception exception) {
        exception.printStackTrace();
        return new ResponseEntity<>(CommonResponse.onFailure(HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 에러가 발생했습니다."), null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
