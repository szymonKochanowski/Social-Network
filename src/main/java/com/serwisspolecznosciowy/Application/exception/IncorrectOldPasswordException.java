package com.serwisspolecznosciowy.Application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class IncorrectOldPasswordException extends RuntimeException {

    public IncorrectOldPasswordException(String message) {
        super(message);
    }
}
