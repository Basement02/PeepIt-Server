package com.b02.peep_it.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestSocialLoginDto {
    private String provider;
    private String idToken;
}

