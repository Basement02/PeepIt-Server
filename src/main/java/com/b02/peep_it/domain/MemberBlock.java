package com.b02.peep_it.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberBlock extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_block_id")
    private Long id; // 회원 차단 고유 ID

    @ManyToOne(optional = false)
    @JoinColumn(name = "blocker_id")
    private Member blockerId; // 차단한 회원 고유 ID

    @ManyToOne(optional = false)
    @JoinColumn(name = "blocked_id")
    private Member blockedId; // 차단된 회원 고유 ID
}
