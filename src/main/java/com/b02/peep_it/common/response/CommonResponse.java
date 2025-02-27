package com.b02.peep_it.common.response;

import com.b02.peep_it.common.exception.CustomError;
import com.b02.peep_it.common.exception.ExceptionDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

public record CommonResponse<T>(
        boolean success,
        @Nullable T data,
        @Nullable ExceptionDto error
) {

    public static <T> ResponseEntity<CommonResponse<T>> ok(@Nullable final T data) {
        return ResponseEntity.ok(new CommonResponse<>(true, data, null));
    }

    public static <T> ResponseEntity<CommonResponse<T>> created(@Nullable final T data) {
        return  ResponseEntity.status(HttpStatus.CREATED).body(new CommonResponse<>(true, data, null));
    }

    public static <T> ResponseEntity<CommonResponse<T>> failed(final CustomError customError) {
        return ResponseEntity.status(customError.getStatus()).body(new CommonResponse<>(false, null, ExceptionDto.of(customError)));
    }

    public static <T> ResponseEntity<CommonResponse<T>> onFailure(final CustomError customError, @Nullable final T data) {
        return ResponseEntity.status(customError.getStatus()).body(new CommonResponse<>(false, data, ExceptionDto.of(customError)));
    }

    public static <T> ResponseEntity<CommonResponse<T>> exception(final Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CommonResponse<>(false, null, ExceptionDto.exception(e)));
    }

    public static <T> ResponseEntity<CommonResponse<T>> onException(final Exception e, @Nullable final T data) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CommonResponse<>(false, data, ExceptionDto.exception(e)));
    }
}