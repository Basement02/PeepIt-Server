package com.b02.peep_it.common.response;

import com.b02.peep_it.common.exception.CustomError;
import com.b02.peep_it.common.exception.ExceptionDto;
import org.springframework.lang.Nullable;

public record ApiResponse<T>(
        boolean success,
        @Nullable T data,
        @Nullable ExceptionDto error
) {

    public static <T> ApiResponse<T> ok(@Nullable final T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> created(@Nullable final T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> failed(final CustomError customError) {
        return new ApiResponse<>(false, null, ExceptionDto.of(customError));
    }

    public static <T> ApiResponse<T> onFailure(final CustomError customError, @Nullable final T data) {
        return new ApiResponse<>(false, data, ExceptionDto.of(customError));
    }
}
