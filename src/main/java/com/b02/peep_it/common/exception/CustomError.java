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
    404
    -01: 존재하지 않는 핍입니다
     */
    PEEP_NOT_FOUND(HttpStatus.NOT_FOUND, "40401", "존재하지 않는 핍입니다"),

    /*
    409
    -01: 이미 사용 중인 아이디입니다
    -02: 이미 사용 중인 전화번호입니다
     */
    DUPLICATED_ID(HttpStatus.CONFLICT, "40901", "이미 사용 중인 아이디입니다"),
    DUPLICATED_PHONE(HttpStatus.CONFLICT, "40902", "이미 사용 중인 전화번호입니다"),

    ;

    @Schema(description = "HTTP 상태 코드", example = "404")
    private final HttpStatus status;
    @Schema(description = "에러 코드", example = "40401")
    private final String code;
    @Schema(description = "에러 메시지", example = "존재하지 않는 핍입니다")
    private final String message;
}
