package com.dgladyshev.deadcodedetector.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Request body or parameters are not valid.")
    @ExceptionHandler(javax.validation.ConstraintViolationException.class)
    public void validationError() {
        //nothing to do
    }

}
