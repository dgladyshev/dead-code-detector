package com.dgladyshev.deadcodedetector.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InspectionAlreadyExistsException extends RuntimeException {

    private static final String MESSAGE = "Inspection for that branch and that repository has "
            + "already been created. Use inspections/refresh endpoint or "
            + "choose another branch to inspect.";

    public InspectionAlreadyExistsException() {
        super(MESSAGE);
    }

}
