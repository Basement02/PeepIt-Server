package com.b02.peep_it.dto.token;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReTokenResponseDto {
    String accessToken;
    String refreshToken;
}
