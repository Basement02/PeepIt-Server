package com.b02.peep_it.service;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.exception.CustomError;
import com.b02.peep_it.common.security.token.CustomUserDetails;
import com.b02.peep_it.common.util.AuthUtils;
import com.b02.peep_it.domain.Member;
import com.b02.peep_it.domain.MemberSocial;
import com.b02.peep_it.domain.PushSetting;
import com.b02.peep_it.domain.TermsAgreement;
import com.b02.peep_it.domain.constant.CustomProvider;
import com.b02.peep_it.dto.RequestSignUpDto;
import com.b02.peep_it.dto.RequestSocialLoginDto;
import com.b02.peep_it.dto.ResponseLoginDto;
import com.b02.peep_it.dto.member.CommonMemberDto;
import com.b02.peep_it.repository.MemberRepository;
import com.b02.peep_it.repository.MemberSocialRepository;
import com.b02.peep_it.common.util.JwtUtils;
import com.b02.peep_it.repository.PushSettingRepository;
import com.b02.peep_it.repository.TermsAgreementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final JwtUtils jwtUtils;
    private final AuthUtils authUtils;
    private final MemberRepository memberRepository;
    private final MemberSocialRepository memberSocialRepository;
    private final TermsAgreementRepository termsAgreementRepository;
    private final PushSettingRepository pushSettingRepository;

    private static final String DEFAULT_PROFILE_IMG = "추후수정필요 프로필 이미지 고정값";

    /*
    - 최초: register token 발급
        - provider에 따라 idtoken 유효성 검증
        - 기존 회원 정보와 중복 확인 (idtoken)
        - provider, providerId(idtoken)으로 register token 생성
        - response body의 data-registerToken에 담아 반환
    - 재: access/refresh token 발급
        - member 객체 조회
        - access/refresh 토큰 발급

    error code: message
    - 40101: 유효하지 않은 소셜 계정입니다
    - 50000: 서버 내부 오류가 발생했습니다
     */
    public ResponseEntity<CommonResponse<ResponseLoginDto>> getRegisterToken(RequestSocialLoginDto requestDto) {
        Boolean isMember = Boolean.FALSE;
        String registerToken = "";
        String accessToken = "";
        String refreshToken = "";
        String name = "";
        String id = "";

        // idtoken에서 고유 id 추출
        String providerId = jwtUtils.getSocialUid(requestDto.provider(), requestDto.idToken());

        // 기존 회원과 provider 고유 id 중복 확인
        Optional<MemberSocial> memberSocial = memberSocialRepository.findByProviderAndProviderId(requestDto.provider().getCode(), providerId);

        // 기존 회원은 access/refresh token 발급 (로그인)
        if (memberSocial.isPresent()) {
            isMember = Boolean.TRUE;
            Optional<Member> memberOptional = memberRepository.findByMemberSocial(memberSocial.get());
            Member member = memberOptional.get();
            CommonMemberDto commonMemberDto = CommonMemberDto.builder()
                    .id(member.getId())
                    .role(member.getRole())
                    .name(member.getNickname())
                    .build();
            accessToken = jwtUtils.createAccessToken(commonMemberDto);
            refreshToken = jwtUtils.createRefreshToken(commonMemberDto);
            name = member.getNickname();
            id = member.getId();
        }
        // 신규 회원은 register token 발급 (가입 대기)
        else {
            // register token 생성
            registerToken = jwtUtils.createRegisterToken(requestDto.provider().getCode(), providerId);
        }
        return CommonResponse.created(ResponseLoginDto.builder()
                .isMember(isMember)
                .registerToken(registerToken)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .name(name)
                .id(id)
                .build());
    }

    /*
    사용자 고유 id 중복 확인
     */
    public ResponseEntity<CommonResponse<Object>> isIdDuplicated(String id) {
        Optional<Member> memberOptional = memberRepository.findById(id);
        if (memberOptional.isPresent()) {
            return CommonResponse.failed(CustomError.DUPLICATED_ID);
        }
        return CommonResponse.ok(null);
    }

    public ResponseEntity<CommonResponse<Object>> isPhoneDuplicated(String phone) {
        Optional<Member> memberOptional = memberRepository.findByPhone(phone);
        if (memberOptional.isPresent()) {
            return CommonResponse.failed(CustomError.DUPLICATED_PHONE);
        }
        return CommonResponse.ok(null);
    }

    /*
    신규 계정 생성
     */
    public ResponseEntity<CommonResponse<ResponseLoginDto>> createAccount(RequestSignUpDto requestDto) {
        // 레지스터 토큰에서 사용자 정보 추출
        CustomUserDetails userDetails = authUtils.getPrincipal();
        CustomProvider provider = CustomProvider.valueOf(userDetails.getProvider());
        String providerId = userDetails.getProviderId();

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
                .phone(requestDto.phone())
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

    /*
    전화번호 인증
     */
}