package com.b02.peep_it.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Alarms {

    INAPP("INAPP", "인앱"),
    PUSH("PUSH", "푸시"),
    POPUP("POPUP", "팝업"),
    ;

    private final String code;
    private final String description;
}
