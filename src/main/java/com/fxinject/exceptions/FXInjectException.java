package com.fxinject.exceptions;

public class FXInjectException extends RuntimeException {
    public FXInjectException(String message) {
        super(message);
    }

    public FXInjectException(String message, Throwable cause) {
        super(message, cause);
    }
}