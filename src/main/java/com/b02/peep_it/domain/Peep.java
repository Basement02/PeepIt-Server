package com.b02.peep_it.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Peep extends BaseTimeEntity {

    private static final Logger log = LoggerFactory.getLogger(Peep.class);
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "peep_id")
    private Long id; // 핍 고유 ID

    @Column(nullable = false, name = "active_time")
    private LocalDateTime activeTime; // 활성화 기준 시각

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "code")
    private State code; // 법정동 코드

    @Column(nullable = false, name = "town")
    private String town; // 법정동명

    @Column(nullable = false, length = 1024, name = "image_url")
    private String imageUrl; // 이미지 or 영상 url

    @Column(nullable = false, name = "is_video")
    private Boolean isVideo;

    @Column(nullable = false)
    private String content; // 본문

    @Column(nullable = false, name = "is_edited")
    private Boolean isEdited; // 수정됨

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private Member member; // 회원 고유 ID

    @OneToOne(mappedBy = "peep")
    private PeepLocation peepLocation; // 단순 조회용 필드 (핍 위치)

    @OneToMany(mappedBy = "peep")
    private List<PeepReSticker> peepReStickerList; //  단순 조회용 필드 (핍 반응)

    @OneToMany(mappedBy = "peep")
    private List<Chat> chatList; // 단순 조회용 필드 (댓글)

    /*
    신규 핍 생성
     */
    @Builder
    public Peep(State code, String town, String imageUrl,
                                  String content, Member member, Boolean isVideo) {
        this.activeTime = LocalDateTime.now();
        this.code = code;
        this.town = town;
        this.imageUrl = imageUrl;
        this.content = content;
        this.isEdited = false;
        this.member = member;
        this.isVideo = isVideo;
    }

    public Peep updateActiveTime() {
        this.activeTime = LocalDateTime.now();
        return this;
    }

    public Peep updatePeepLocation(PeepLocation peepLocation) {
        this.peepLocation = peepLocation;
        return this;
    }

    /*
    인기도 점수 계산 함수
     */
    public double calculatePopularityScore() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoHoursAgo = now.minusHours(2);

        int totalChat = chatList != null ? chatList.size() : 0;
        int totalReact = peepReStickerList != null ? peepReStickerList.size() : 0;

        int recentChat = chatList != null
                ? (int) chatList.stream().filter(chat -> chat.getCreatedAt().isAfter(twoHoursAgo)).count()
                : 0;

        int recentReact = peepReStickerList != null
                ? (int) peepReStickerList.stream().filter(r -> r.getCreatedAt().isAfter(twoHoursAgo)).count()
                : 0;

        double rawScore = (totalChat * 0.6 + totalReact * 0.4) + (recentChat * 0.6 + recentReact * 0.6);

        double a = 0.4;
        double b = 8.0;
        double sigmoid = 10.0 / (1.0 + Math.exp(-a * (rawScore - b)));

        // 강제 1~10 범위로 클리핑
        double normalizedScore = Math.min(10.0, Math.max(1.0, sigmoid));

        return normalizedScore;
    }
}