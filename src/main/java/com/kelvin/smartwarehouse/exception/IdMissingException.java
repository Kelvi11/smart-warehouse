package com.kelvin.smartwarehouse.exception;

public class IdMissingException extends RuntimeException{

    private final static String message = "Id shouldn't be null or empty!";

    public IdMissingException() {
        super(message);
    }
}
