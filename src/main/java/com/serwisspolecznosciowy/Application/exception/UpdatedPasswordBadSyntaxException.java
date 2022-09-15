package com.serwisspolecznosciowy.Application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UpdatedPasswordBadSyntaxException extends RuntimeException {

    public UpdatedPasswordBadSyntaxException(String message) {
        super(message);
    }
}
