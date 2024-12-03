package com.b02.peep_it.domain;

import com.nimbusds.openid.connect.sdk.claims.Gender;

public class CustomGender extends Gender {
    public static final CustomGender OTHER = new CustomGender("other");

    public CustomGender(String value) {
        super(value);
    }
}
