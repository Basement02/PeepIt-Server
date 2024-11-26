package com.b02.peep_it.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PeepReSticker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "peep_re_sticker_id")
    private Long id; // 핍 반응 고유 ID

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private Member member; // 회원 고유 ID

    @ManyToOne(optional = false)
    @JoinColumn(name = "re_sticker_id")
    private ReSticker reSticker; // 스티커 고유 ID

    @ManyToOne(optional = false)
    @JoinColumn(name = "peep_id")
    private Peep peep; // 핍 고유 ID
}
