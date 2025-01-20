package com.b02.peep_it.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final CustomError error;
    public ApiException(CustomError error) {
        super(error.getCode());
        this.error = error;
    }
}
