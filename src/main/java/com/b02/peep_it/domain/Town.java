package com.b02.peep_it.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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
    @JoinColumn(name = "state")
    private State state; // 법정동 코드

    @Builder
    public Town(Member member, State state) {
        this.member = member;
        this.state = state;
    }

    public Town updateTown(State state) {
        this.state = state;
        return this;
    }

    public String getStateName() {
        return this.state.getName();
    }
}
