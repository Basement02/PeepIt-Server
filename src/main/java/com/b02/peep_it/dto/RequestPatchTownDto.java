package com.b02.peep_it.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RequestPatchTownDto(
        @NotNull
        @Schema(description = "10자리 법정동 코드", example = "1111010100")
        @Pattern(regexp = "^[0-9]{10}$", message = "법정동 코드는 10자리 숫자여야 합니다.")
        String legalDistrictCode // 법정동 코드
) {
}
