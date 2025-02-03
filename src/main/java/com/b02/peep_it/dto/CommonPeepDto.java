package com.b02.peep_it.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CommonPeepDto(
        @NotNull Long peepId,
        @NotNull String memberId,
        String legalDistrictCode,
        String imageUrl,
        String content,
        Boolean isEdited,
        String profileUrl,
        String uploadAt,
        Integer stickerNum,
        Integer chatNum
        ) {
}
