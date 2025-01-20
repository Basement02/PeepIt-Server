package com.b02.peep_it.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.lang.Nullable;

@Builder
public record ResponseSocialLoginDto(
        @NotNull Boolean isMember,
        @Nullable String registerToken,
        @Nullable String accessToken,
        @Nullable String refreshToken,
        @Nullable String name,
        @Nullable String id
) {
}