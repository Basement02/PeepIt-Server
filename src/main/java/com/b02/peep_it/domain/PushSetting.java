package com.b02.peep_it.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushSetting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "push_setting_id")
    private Long id; // 알림 설정 고유 ID

    @Column(name = "push_peep_chat", nullable = false)
    private Boolean pushPeepChat; // 핍_새로운 채팅

    @Column(name = "push_peep_sticker", nullable = false)
    private Boolean pushPeepSticker; // 핍_새로운 반응

    @Column(name = "push_peep_mypop", nullable = false)
    private Boolean pushPeepMypop; // 핍_인기 핍 선정

    @Column(name = "push_peep_nowpop", nullable = false)
    private Boolean pushPeepNowpop; // 핍_인기 핍 추천

    @Column(name = "push_peep_news", nullable = false)
    private Boolean pushPeepNews; // 핍_새로운 소식

    @Column(name = "push_service_news", nullable = false)
    private Boolean pushServiceNews; // 서비스_새로운 소식

    @Column(name = "push_service_alarm", nullable = false)
    private Boolean pushServiceAlarm; // 서비스_기타(신고 등)

    @Column(name = "push_service_marketing", nullable = false)
    private Boolean pushServiceMarketing; // 서비스_마케팅

    @OneToOne(optional = true)
    @JoinColumn(name = "member_id")
    private Member member; // 회원 고유 ID

    @Builder
    public PushSetting(Member member) {
        this.pushPeepChat = true;
        this.pushPeepSticker = true;
        this.pushPeepMypop = true;
        this.pushPeepNowpop = true;
        this.pushPeepNews = true;
        this.pushServiceNews = true;
        this.pushServiceAlarm = true;
        this.pushServiceMarketing = true;
        this.member = member;
    }
}
