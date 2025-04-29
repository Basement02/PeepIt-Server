package com.b02.peep_it.dto.member;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ResponseCommonMemberDto(
        @NotNull String id,
        String role,
        String gender, // 성별
        String name, // 닉네임
        String town, // 동네이름
        String legalCode, // 법정동 이름
        String profile, // 프로필 url,
        Boolean isAgree // 마케팅 약관 동의 여부
) {
}