package com.dgladyshev.deadcodedetector.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class InspectionAlreadyExistsException extends RuntimeException {

    public InspectionAlreadyExistsException(String message) {
        super(message);
    }

}
