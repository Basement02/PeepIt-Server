package com.b02.peep_it.dto.peep;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CommonPeepDto(
        @NotNull Long peepId,
        @NotNull String memberId,
        @NotNull String town,
        @NotNull String legalDistrictCode,
        @NotNull String imageUrl,
        @NotNull String content,
        @NotNull Boolean isEdited,
        @NotNull String profileUrl,
        @NotNull Boolean isActive, // 활성화(24시간 이내) 여부
        @NotNull String uploadAt,
        @NotNull Integer stickerNum,
        @NotNull Integer chatNum
        ) {
}
