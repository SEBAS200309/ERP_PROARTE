package com.proarte.erp.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Credenciales incorrectas");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
