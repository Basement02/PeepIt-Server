package com.b02.peep_it.common.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CustomError {

    // need to custom
    NEED_TO_CUSTOM("00000", "NEED TO ERROR CUSTOM!");

    private final String code;
    private final String message;
}
