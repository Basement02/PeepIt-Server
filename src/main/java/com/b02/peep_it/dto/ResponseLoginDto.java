package com.b02.peep_it.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.lang.Nullable;

@Builder
public record ResponseLoginDto(
        @NotNull Boolean isMember,
        @Nullable String registerToken,
        @Nullable String accessToken,
        @Nullable String refreshToken,
        @Nullable String name, // 닉네임
        @Nullable String id // 핍잇 사용자 고유 ID
) {
}