package com.b02.peep_it.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReSticker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "re_sticker_id")
    private Long id; // 스티커 고유 ID

    @Column(nullable = false)
    private String title; // 스티커명

    @Column(nullable = false, name = "image_url")
    private String imageUrl; // 이미지 url
}
