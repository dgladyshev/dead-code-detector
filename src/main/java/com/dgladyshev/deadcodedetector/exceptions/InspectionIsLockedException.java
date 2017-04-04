package com.dgladyshev.deadcodedetector.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.LOCKED)
public class InspectionIsLockedException extends RuntimeException {

    private static final String MESSAGE = "Inspection is locked for any changes until code analysis would be completed.";

    public InspectionIsLockedException() {
        super(MESSAGE);
    }

}
