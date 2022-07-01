package com.kelvin.smartwarehouse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@ControllerAdvice
public class EntityWithIdNotFoundExceptionHandler {

    @ExceptionHandler(value = {EntityWithIdNotFoundException.class})
    public ResponseEntity handleInvalidParameterException(EntityWithIdNotFoundException e){

        ApiException apiException = new ApiException(
                e.getMessage(),
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(apiException, HttpStatus.BAD_REQUEST);
    }
}
