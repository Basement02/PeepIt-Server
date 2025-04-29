package com.b02.peep_it.dto.peep;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CommonPeepDto(
        @NotNull Long peepId,
        @NotNull String memberId,
        @NotNull String town,
        @NotNull Double longitude,
        @NotNull Double latitude,
        @NotNull String building,
        @NotNull String imageUrl,
        @NotNull String content,
        @NotNull Boolean isEdited,
        @NotNull String profileUrl,
        @NotNull Boolean isActive, // 활성화(24시간 이내) 여부
        @NotNull String uploadAt,
        @NotNull Integer stickerNum,
        @NotNull Integer chatNum,
        @NotNull Double popularity, // 인기도
        @NotNull Boolean isVideo // 비디오(true) / 이미지(false)
        ) {
}
