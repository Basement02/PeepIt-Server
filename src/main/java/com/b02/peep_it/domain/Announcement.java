package com.b02.peep_it.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Announcement extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announcement_id")
    private Long id; // 공지 고유 ID

    @Column(nullable = false)
    private String category; // 분류

    @Column(nullable = false)
    private String title; // 제목

    @Column(nullable = true)
    private String image; // 이미지

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private Member member; // 회원 고유 ID
}
