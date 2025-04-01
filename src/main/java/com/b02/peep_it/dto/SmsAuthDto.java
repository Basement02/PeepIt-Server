package com.b02.peep_it.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
public record SmsAuthDto(
        String code,
        int tryCount,
        LocalDateTime requestedAt
) implements Serializable {}
