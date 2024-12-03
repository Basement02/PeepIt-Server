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
    @Column(name = "member_id")
    private String id; // 회원 고유 ID (외래키)

    @OneToOne
    @MapsId
    @JoinColumn
    private Member member; // 회원 고유 ID

    @Column(name = "is_agree", nullable = false)
    private Boolean isAgree; // 마케팅 이용약관 동의 여부

    @Builder
    public TermsAgreement(Member member, Boolean isAgree) {
        this.member = member;
        this.isAgree = isAgree;
    }

    public TermsAgreement setAgree() {
        this.isAgree = true;
        return this;
    }

    public TermsAgreement setDisAgree() {
        this.isAgree = false;
        return this;
    }
}
