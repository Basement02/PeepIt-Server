package com.b02.peep_it.exception;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(CustomError error) {
        super(error);
    }
}
