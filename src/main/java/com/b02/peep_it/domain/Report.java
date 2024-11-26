package com.b02.peep_it.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id; // 신고 고유 ID

    @Column(nullable = false)
    private String content; // 신고 사유

    @ManyToOne(optional = false)
    @JoinColumn(name = "reporting_member_id")
    private Member reportingMember; // 신고자 고유 ID

    @ManyToOne(optional = false)
    @JoinColumn(name = "reported_member_id")
    private Member reportedMember; // 피신고자 고유 ID

    @ManyToOne(optional = false)
    @JoinColumn(name = "report_table_id")
    private ReportTitle reportTitle; // 신고 사유 고유 ID

    @ManyToOne(optional = true)
    @JoinColumn(name = "peep_id")
    private Peep peep; // 신고 핍 고유 ID

    @ManyToOne(optional = true)
    @JoinColumn(name = "chat_id")
    private Chat chat; // 채팅 고유 ID
}
