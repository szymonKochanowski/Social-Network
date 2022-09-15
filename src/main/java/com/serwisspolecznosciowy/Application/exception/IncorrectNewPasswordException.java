package com.serwisspolecznosciowy.Application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IncorrectNewPasswordException extends  RuntimeException{

    public IncorrectNewPasswordException(String message) {
        super(message);
    }
}
