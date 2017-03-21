package com.dgladyshev.deadcodedetector.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @RequestMapping(path = {"/health"}, method = RequestMethod.GET)
    public String health() {
        return "OK";
    }

}
