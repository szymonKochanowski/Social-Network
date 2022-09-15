package com.serwisspolecznosciowy.Application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserForbiddenAccessException extends RuntimeException {

    public UserForbiddenAccessException(String message) {
        super(message);
    }
}
