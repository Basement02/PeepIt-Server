package com.b02.peep_it.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Termination extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "termination_id")
    private Long id; // 탈퇴 요청 고유 ID

    @Column(nullable = false)
    private String content; // 본문 (사용자 작성)

    @ManyToOne
    @JoinColumn(name = "termination_title_id")
    private TerminationTitle terminationTitle; // 탈퇴 사유 고유 ID

    @OneToOne(mappedBy = "termination")
    private Member member; // 양방향 조회용 필드
}
