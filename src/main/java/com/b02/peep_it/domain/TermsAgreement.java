package com.b02.peep_it.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermsAgreement extends BaseTimeEntity {
    @Id
    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member; // 회원 고유 ID

    @Column(name = "service_ver", nullable = false)
    private String serviceVer; // 서비스 이용약관 버전

    @Column(name = "local_ver", nullable = false)
    private String localVer; // 위치기반 이용약관 버전

    @Column(name = "private_ver", nullable = false)
    private String privateVer; // 개인정보 이용약관 버전

    @Column(name = "market_ver", nullable = false)
    private String marketVer; // 마케팅 이용약관 버전

    @Column(name = "is_agree", nullable = false)
    private Boolean isAgree; // 마케팅 이용약관 동의 여부

    @Builder
    public TermsAgreement(Member member, String serviceVer, String localVer, String privateVer, String marketVer, Boolean isAgree) {
        this.member = member;
        this.serviceVer = serviceVer;
        this.localVer = localVer;
        this.privateVer = privateVer;
        this.marketVer = marketVer;
        this.isAgree = isAgree;
    }
}
