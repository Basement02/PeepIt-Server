package com.b02.peep_it.domain;

import com.b02.peep_it.domain.constant.CustomProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSocial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_social_id")
    private Long id; // 소셜로그인 정보 고유 ID

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CustomProvider provider; // 제공자

    @Column(name = "provider_id", nullable = false)
    private String providerId; // 제공받은 식별자

    @Column(nullable = false)
    private String email; // 이메일
}
