package com.b02.peep_it.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum MsgCategory {

    // ACT_회원 활동 알림
    ACT_0("ACT_0", "인기 핍 선정", Arrays.asList(Alarms.INAPP, Alarms.PUSH)),

    // PEEP_회원 핍 알림
    PEEP_0("PEEP_0", "반응(단일)", Arrays.asList(Alarms.INAPP, Alarms.PUSH)),
    PEEP_1("PEEP_1", "반응(다수)", Arrays.asList(Alarms.INAPP, Alarms.PUSH)),
    PEEP_2("PEEP_2", "채팅(단일)", Arrays.asList(Alarms.INAPP, Alarms.PUSH)),
    PEEP_3("PEEP_3", "채팅(다수)", Arrays.asList(Alarms.INAPP, Alarms.PUSH)),

    // PROMO_활동 유도 알림
    PROMO_0("PROMO_0", "인기 핍 추천(아이디)", Arrays.asList(Alarms.INAPP, Alarms.PUSH)),
    PROMO_1("PROMO_1", "인기 핍 추천(동네)", Arrays.asList(Alarms.INAPP, Alarms.PUSH)),

    // NEWS_서비스 소식
    NEWS_0("NEWS_0", "신기능 소개(팝업)", Arrays.asList(Alarms.POPUP)),
    NEWS_1("NEWS_1", "신기능 소개(푸시, 인앱)", Arrays.asList(Alarms.INAPP, Alarms.PUSH)),
    ;

    private final String code;
    private final String description;
    private final List<Alarms> alarms;
}
