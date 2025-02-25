package com.b02.peep_it.common.exception;

import jakarta.validation.constraints.NotNull;

public record ExceptionDto(
        @NotNull String code,
        @NotNull String message
) {

    public static ExceptionDto of(CustomError customError) {
        return new ExceptionDto(
                customError.getCode(),
                customError.getMessage()
        );
    }

    public static ExceptionDto containMessageOf(CustomError customError, String message) {
        return new ExceptionDto(
                customError.getCode(),
                message
        );
    }

    public static ExceptionDto exception(Exception e) {
        return new ExceptionDto(
                "00000",
                e.getMessage()
        );
    }
}
