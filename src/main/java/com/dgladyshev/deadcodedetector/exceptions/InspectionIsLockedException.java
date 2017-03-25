package com.dgladyshev.deadcodedetector.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.LOCKED)
public class InspectionIsLockedException extends RuntimeException {

    public InspectionIsLockedException(String message) {
        super(message);
    }

}
