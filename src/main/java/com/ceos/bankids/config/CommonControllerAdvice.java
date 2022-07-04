package com.ceos.bankids.config;

import com.ceos.bankids.domain.User;
import com.ceos.bankids.exception.BaseException;
import java.io.PrintWriter;
import java.io.StringWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class CommonControllerAdvice {

    private void getExceptionStackTrace(Exception e, @AuthenticationPrincipal User user) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.append("\n==========================!!!TRACE START!!!==========================\n");
        pw.append("uid: " + user.getId() + "\n");
        pw.append(e.getMessage());
        pw.append("\n==================================================================\n");
        log.error(sw.toString());
    }

    @ExceptionHandler(value = BaseException.class)
    public ResponseEntity onKnownException(BaseException baseException,
        @AuthenticationPrincipal User user) {
        getExceptionStackTrace(baseException, user);
        return new ResponseEntity<>(CommonResponse.onFailure(baseException.getResponseMessage()),
            null, baseException.getStatusCode());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity onException(Exception exception, @AuthenticationPrincipal User user) {
        getExceptionStackTrace(exception, user);
        return new ResponseEntity<>(CommonResponse.onFailure("서버 에러가 발생했습니다."), null,
            HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
