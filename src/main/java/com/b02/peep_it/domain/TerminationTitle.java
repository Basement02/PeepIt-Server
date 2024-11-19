package com.b02.peep_it.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TerminationTitle {
    @Id
    @GeneratedValue
    @Column(name = "termination_title_id")
    private Long id; // 탈퇴 사유 고유 ID

    @Column(nullable = false)
    private String title; // 항목 상세
}
