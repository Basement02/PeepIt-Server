package com.b02.peep_it.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Peep extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "peep_id")
    private Long id; // 핍 고유 ID

    @Column(nullable = false, name = "legal_district_code")
    private String legalDistrictCode; // 법정동 코드

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
}
