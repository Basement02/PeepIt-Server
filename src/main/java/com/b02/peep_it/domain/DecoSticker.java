package com.b02.peep_it.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DecoSticker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deco_sticker_id")
    private Long id; // 스티커 고유 ID

    @Column(nullable = false)
    private String title; // 스티커명

    @Column(name = "image_url", nullable = false)
    private String imageUrl; // 이미지 url
}
