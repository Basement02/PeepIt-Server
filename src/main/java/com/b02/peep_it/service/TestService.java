package com.b02.peep_it.service;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.util.AuthUtils;
import com.b02.peep_it.common.util.JwtUtils;
import com.b02.peep_it.domain.Member;
import com.b02.peep_it.domain.MemberSocial;
import com.b02.peep_it.domain.PushSetting;
import com.b02.peep_it.domain.TermsAgreement;
import com.b02.peep_it.domain.constant.CustomProvider;
import com.b02.peep_it.dto.RequestSignUpDto;
import com.b02.peep_it.dto.ResponseLoginDto;
import com.b02.peep_it.dto.member.CommonMemberDto;
import com.b02.peep_it.repository.MemberRepository;
import com.b02.peep_it.repository.MemberSocialRepository;
import com.b02.peep_it.repository.PushSettingRepository;
import com.b02.peep_it.repository.TermsAgreementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TestService {

    private final JwtUtils jwtUtils;
    private final AuthUtils authUtils;
    private final MemberRepository memberRepository;
    private final PushSettingRepository pushSettingRepository;
    private final MemberSocialRepository memberSocialRepository;
    private final TermsAgreementRepository termsAgreementRepository;

    private static final String DEFAULT_PROFILE_IMG = "추후수정필요 프로필 이미지 고정값";

    /*
    가상 계정 생성
     */
    public ResponseEntity<CommonResponse<ResponseLoginDto>> createAccount(RequestSignUpDto requestDto) {
        // mock data 생성
        CustomProvider provider = CustomProvider.TESTER;
        String providerId = "ThisisaMockIdforTesterAccount";

        // 소셜 로그인 객체 생성 & 저장
        MemberSocial memberSocial = MemberSocial.builder()
                .provider(provider)
                .providerId(providerId)
                .build();

        memberSocialRepository.save(memberSocial);


        // 회원 객체 생성 & 저장
        log.info("‼\uFE0F프로필 사진 기본 이미지 경로 변경 필요");
        Member member = Member.builder()
                .id(requestDto.id())
                .nickname(requestDto.nickname())
                .profileImg(DEFAULT_PROFILE_IMG)
                .birth(requestDto.birth())
                .gender(requestDto.gender())
                .memberSocial(memberSocial)
                .build();

        memberRepository.save(member);

        // 약관 동의 객체 생성 & 저장
        TermsAgreement termsAgreement = TermsAgreement.builder()
                .member(member)
                .isAgree(requestDto.isAgree())
                .build();

        termsAgreementRepository.save(termsAgreement);

        // 알림 설정 동의 객체 생성 & 저장
        // 기본값: 모든 알림 on
        PushSetting pushSetting = PushSetting.builder()
                .member(member)
                .build();

        pushSettingRepository.save(pushSetting);

        // 로그인
        Boolean isMember = Boolean.TRUE;
        String registerToken = "";
        CommonMemberDto commonMemberDto = CommonMemberDto.builder()
                .id(member.getId())
                .role(member.getRole())
                .name(member.getNickname())
                .build();
        String accessToken = jwtUtils.createAccessToken(commonMemberDto);
        String refreshToken = jwtUtils.createRefreshToken(commonMemberDto);
        String name = member.getNickname();
        String id = member.getId();

        return CommonResponse.created(ResponseLoginDto.builder()
                .isMember(isMember)
                .registerToken(registerToken)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .name(name)
                .id(id)
                .build());
    }
}
