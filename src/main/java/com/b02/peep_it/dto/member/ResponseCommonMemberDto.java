package com.b02.peep_it.dto.member;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ResponseCommonMemberDto {
    @NotNull String id;
    String role;
    String gender; // 성별
    String name; // 닉네임
    String town; // 동네이름
    String legalCode; // 법정동 이름
    String profile; // 프로필 url,
    Boolean isAgree; // 마케팅 약관 동의 여부

    @Builder.Default
    Boolean isBlocked = false; // 차단 여부
}