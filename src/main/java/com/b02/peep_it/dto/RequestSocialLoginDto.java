package com.b02.peep_it.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RequestSocialLoginDto(
        @NotNull String provider,
        @NotNull String idToken
) {
}
