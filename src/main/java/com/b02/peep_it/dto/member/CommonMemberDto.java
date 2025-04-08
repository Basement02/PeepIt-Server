package com.b02.peep_it.dto.member;

import com.b02.peep_it.domain.constant.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CommonMemberDto(
        @NotNull String id,
        String role,
        String gender, // 성별
        String name, // 닉네임
        String town, // 동네이름
        String profile // 프로필 url
) {
}
