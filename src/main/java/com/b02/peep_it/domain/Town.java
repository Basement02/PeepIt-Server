package com.b02.peep_it.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Town extends BaseTimeEntity {
    @Id
    @OneToOne(optional = false)
    @JoinColumn(name = "member_id")
    private Member member; // 회원 고유 ID

    @ManyToOne(optional = false)
    @JoinColumn(name = "code")
    private State state; // 법정동 코드
}
