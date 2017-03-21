package com.dgladyshev.deadcodedetector.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Data
@AllArgsConstructor
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValueOutOfRangeException extends RuntimeException {

	private String minValue;

	private String maxValue;

}
