package com.dgladyshev.deadcodedetector.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsupportedParameterException extends RuntimeException {

    public UnsupportedParameterException(String message) {
        super(message);
    }

}
