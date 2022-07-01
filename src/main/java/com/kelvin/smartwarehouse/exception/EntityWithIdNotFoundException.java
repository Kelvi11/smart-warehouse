package com.kelvin.smartwarehouse.exception;

public class EntityWithIdNotFoundException extends RuntimeException{

    public EntityWithIdNotFoundException(String message) {
        super(message);
    }

    public EntityWithIdNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
