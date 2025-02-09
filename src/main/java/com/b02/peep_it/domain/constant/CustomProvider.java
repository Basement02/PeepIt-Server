package com.b02.peep_it.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomProvider {

    KAKAO("KAKAO", "카카오 인증 회원"),
    NAVER("NAVER", "네이버 인증 회원"),
    APPLE("APPLE", "애플 인증 회원"),
    TESTER("TESTER", "테스터: 가상 회원")
    ;

    private final String code;
    private final String description;
}