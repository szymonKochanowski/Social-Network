package com.serwisspolecznosciowy.Application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PostEmptyBodyException extends RuntimeException {

    public PostEmptyBodyException(String message) {
        super(message);
    }

}
