package com.b02.peep_it.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    CERT("CERTIFIED", "인증 회원"),
    UNCERT("UNCERTIFIED", "미인증 회원"),
    MAN("MANAGER", "관리자"),
    DEV("DEVELOPER", "개발자")
    ;

    private final String code;
    private final String description;
}
