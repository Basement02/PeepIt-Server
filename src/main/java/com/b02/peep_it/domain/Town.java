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
    @Column(name = "member_id")
    private String id; // 회원 고유 ID (외래키)

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn
    private Member member; // 회원 고유 ID

    @ManyToOne(optional = true)
    @JoinColumn(name = "code1")
    private State code1; // 법정동 코드 1

    @ManyToOne(optional = true)
    @JoinColumn(name = "code2")
    private State code2; // 법정동 코드 2
}
