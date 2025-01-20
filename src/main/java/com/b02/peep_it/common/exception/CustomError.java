package com.b02.peep_it.common.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CustomError {

    // need to custom
    NEED_TO_CUSTOM("00000", "NEED TO ERROR CUSTOM!"),

    /*
    409
    -01: 이미 사용 중인 아이디입니다
    -02: 이미 사용 중인 전화번호입니다
     */
    DUPLICATED_ID("40901", "이미 사용 중인 아이디입니다"),
    DUPLICATED_PHONE("40902", "이미 사용 중인 전화번호입니다"),

    ;

    private final String code;
    private final String message;
}
