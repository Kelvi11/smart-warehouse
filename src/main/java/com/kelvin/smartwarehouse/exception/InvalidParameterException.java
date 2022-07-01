package com.kelvin.smartwarehouse.exception;

public class InvalidParameterException extends RuntimeException{

    private final static String message = "Id shouldn't be null or empty!";

    public InvalidParameterException() {
        super(message);
    }
}
