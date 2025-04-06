package com.b02.peep_it.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Schema(description = "API 에러 코드 목록")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum CustomError {

    // need to custom
    NEED_TO_CUSTOM(HttpStatus.INTERNAL_SERVER_ERROR,"00000", "NEED TO ERROR CUSTOM!"),

    /*
    401
    - 01: 유효하지 않은 social id token입니다
    - 02: 유효하지 않은 계정입니다
    - 03: 유효하지 않은 sms code입니다
     */
    ID_TOKEN_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "40101", "유효하지 않은 social id token입니다"),
    MEMBER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "40102", "유효하지 않은 계정입니다"),
    WRONG_SMS(HttpStatus.UNAUTHORIZED, "40103", "유효하지 않은 sms code입니다"),

    /*
    404
    -01: 존재하지 않는 핍입니다
    -02: 사용자의 동네 정보가 존재하지 않습니다
    -03: 존재하지 않는 법정동코드입니다
    -04: 존재하지 않는 사용자입니다
     */
    PEEP_NOT_FOUND(HttpStatus.NOT_FOUND, "40401", "존재하지 않는 핍입니다"),
    TOWN_NOT_FOUND(HttpStatus.NOT_FOUND, "40402", "사용자의 동네 정보가 존재하지 않습니다"),
    STATE_NOT_FOUND(HttpStatus.NOT_FOUND, "40403", "존재하지 않는 법정동코드입니다"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "40404", "존재하지 않는 사용자입니다"),

    /*
    409
    -01: 이미 사용 중인 아이디입니다
    -02: 이미 사용 중인 전화번호입니다
     */
    DUPLICATED_ID(HttpStatus.CONFLICT, "40901", "이미 사용 중인 아이디입니다"),
    DUPLICATED_PHONE(HttpStatus.CONFLICT, "40902", "이미 사용 중인 전화번호입니다"),

    /*
    410
    -01: 인증번호가 만료되었거단 존재하지 않습니다
     */
    SMS_EXPIRED(HttpStatus.GONE, "41001", "인증번호가 만료되었거단 존재하지 않습니다"),

    /*
    429
    -01: 인증 시도 횟수를 초과하였습니다
     */
    OVER_MAX_TRY(HttpStatus.TOO_MANY_REQUESTS, "42901", "인증 시도 횟수를 초과하였습니다"),

    ;

    @Schema(description = "HTTP 상태 코드", example = "404")
    private final HttpStatus status;
    @Schema(description = "에러 코드", example = "40401")
    private final String code;
    @Schema(description = "에러 메시지", example = "존재하지 않는 핍입니다")
    private final String message;
}
