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

    @Column(nullable = false, name = "image_url")
    private String imageUrl; // 이미지 or 영상 url

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
                                  String content, Member member) {
        this.activeTime = LocalDateTime.now();
        this.code = code;
        this.town = town;
        this.imageUrl = imageUrl;
        this.content = content;
        this.isEdited = false;
        this.member = member;
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
        log.info("추후 수정 필요");
        // 전체 좋아요 수
        int likeCount = peepReStickerList != null ? peepReStickerList.size() : 0;
        // 전체 댓글 수
        int commentCount = chatList != null ? chatList.size() : 0;

        // 사용자 반응(좋아요/댓글)이 있는 경우 1, 없으면 0
        int hasUserLiked = peepReStickerList.stream()
                .anyMatch(reaction -> reaction.getMember().getId().equals(this.member.getId())) ? 1 : 0;
        int hasUserCommented = chatList.stream()
                .anyMatch(comment -> comment.getMember().getId().equals(this.member.getId())) ? 1 : 0;

        // 인기도 공식 적용 (Plike * Vlike) + (Pcomment * Vcomment)
        return (hasUserLiked * likeCount) + (hasUserCommented * commentCount);
    }
}