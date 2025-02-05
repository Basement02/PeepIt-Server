package com.b02.peep_it.dto.member;

import com.b02.peep_it.domain.constant.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CommonMemberDto(
        @NotNull String id,
        Role role,
        String name
) {
}
