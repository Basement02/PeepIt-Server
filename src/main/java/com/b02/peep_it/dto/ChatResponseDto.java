package com.b02.peep_it.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatResponseDto(
        String uid,
        String nickname,
        String imgUrl,
        String content,
        String sentAt
) {
}
