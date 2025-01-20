package com.b02.peep_it.dto;

import com.b02.peep_it.domain.constant.CustomProvider;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RequestSocialLoginDto(
        @NotNull CustomProvider provider,
        @NotNull String idToken
) {
}
