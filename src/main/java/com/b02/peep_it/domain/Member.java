package com.b02.peep_it.domain;

import com.b02.peep_it.domain.constant.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id; // 회원 고유 ID

    @Column(nullable = false)
    private String nickname; // 닉네임

    @Column(nullable = false)
    private String phone; // 전화번호

    @Column(name = "profile_img", nullable = true)
    private String profileImg; // 프로필 이미지

    @Column(nullable = true)
    private LocalDateTime birth; // 생년월일

    @Column(nullable = true)
    private CustomGender gender; // 성별

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role; // 회원 분류

    @Column(name = "auth_at", nullable = true)
    private LocalDateTime authAt; // 인증 일자(전화번호)

    @Column(name = "is_terminated", nullable = false)
    private Boolean isTerminated; // 탈퇴 요청 여부

    @OneToOne(optional = false)
    @JoinColumn(name = "member_social_id", nullable = false)
    private MemberSocial memberSocial;
}
