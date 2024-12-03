package com.b02.peep_it.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class State {
    @Id
    private final String code; // 법정동 코드

    @Column(nullable = false)
    private final String name; // 법정동명

    @Builder
    public State(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
