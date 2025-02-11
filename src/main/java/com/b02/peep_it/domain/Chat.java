package com.b02.peep_it.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chat extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Long id; // 채팅 고유 ID

    @Column(nullable = false)
    private String content; // 본문

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "peep_id")
    private Peep peep; // 핍 고유 ID

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 회원 고유 ID
}
