package com.dgladyshev.deadcodedetector.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MalformedRepositoryUrl extends RuntimeException {

    public MalformedRepositoryUrl(String message) {
        super(message);
    }
}
