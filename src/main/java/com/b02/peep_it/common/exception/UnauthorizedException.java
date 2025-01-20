package com.b02.peep_it.common.exception;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(CustomError error) {
        super(error);
    }
}
