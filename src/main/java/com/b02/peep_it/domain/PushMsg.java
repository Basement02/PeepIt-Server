package com.b02.peep_it.domain;

import com.b02.peep_it.domain.constant.MsgCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushMsg extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "push_msg_id")
    private Long id; // 알림 메시지 고유 ID

    @Column(nullable = false)
    private MsgCategory category; // 메시지 유형

    @Column(nullable = false)
    private String title; // 제목

    @Column(name = "sub_title", nullable = true)
    private String body; // 내용

    @Column(nullable = true)
    private String image; // 핍 썸네일

    @Column(name = "response_msg_id", nullable = false)
    private String responseMsgId; // fcm 메시지 고유 ID

    @ManyToOne(optional = true)
    @JoinColumn(name = "peep_id")
    private Peep peep; // 핍 고유 ID

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private Member member; // 회원 고유 ID
}
