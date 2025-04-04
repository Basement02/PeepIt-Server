package com.b02.peep_it.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CommonTownDto(
        @NotNull String legalCode, // 법정동 코드
        @NotNull String name // 동네 이름
) {
}
