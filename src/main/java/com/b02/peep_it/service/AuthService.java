package com.b02.peep_it.service;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.exception.CustomError;
import com.b02.peep_it.common.security.token.CustomUserDetails;
import com.b02.peep_it.common.util.AuthUtils;
import com.b02.peep_it.domain.*;
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
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang.math.RandomUtils.nextInt;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    @Value("${coolsms.api.key}")
    String apiKey;
    @Value("${coolsms.api.secret}")
    String apiSecret;
    @Value("${coolsms.api.number}")
    String sender;
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
    @Transactional
    public ResponseEntity<CommonResponse<ResponseLoginDto>> getRegisterToken(RequestSocialLoginDto requestDto) throws Exception {
        Boolean isMember = Boolean.FALSE;
        String registerToken = "";
        String accessToken = "";
        String refreshToken = "";
        String name = "";
        String id = "";
        CustomProvider provider = CustomProvider.valueOf(requestDto.provider());

        // idtoken에서 고유 id 추출
        String socialUid = jwtUtils.getSocialUid(provider, requestDto.idToken());

        // 기존 회원과 provider 고유 id 중복 확인
        Optional<MemberSocial> memberSocial = memberSocialRepository.findByProviderAndProviderId(provider, socialUid);

        // 기존 회원은 access/refresh token 발급 (로그인)
        if (memberSocial.isPresent()) {
            isMember = Boolean.TRUE;
            Optional<Member> memberOptional = memberRepository.findByMemberSocial(memberSocial.get());

            // member가 존재하지 않으면 예외 발생 (메시지 포함)
            Member member = memberOptional.orElseThrow(() ->
                    new Exception("Member 정보가 존재하지 않습니다. socialId: " + memberSocial.get().getProviderId())
            );

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
            registerToken = jwtUtils.createRegisterToken(provider.getCode(), requestDto.idToken());
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
    @Transactional
    public ResponseEntity<CommonResponse<ResponseLoginDto>> createAccount(RequestSignUpDto requestDto) {
        log.info("🟢 createAccount 시작 - requestDto: {}", requestDto);

        // 레지스터 토큰에서 사용자 정보 추출
        CustomUserDetails userDetails = authUtils.getPrincipal();
        log.info("🔹 사용자 정보 추출 완료 - provider: {}, providerId: {}", userDetails.getProvider(), userDetails.getProviderId());

        CustomProvider provider = CustomProvider.valueOf(userDetails.getProvider());
        String providerId = userDetails.getProviderId();

        // idtoken 유효성 검증
        if (!jwtUtils.validateIdToken(provider, providerId)) {
            log.warn("⚠ 유효하지 않은 id token - provider: {}, providerId: {}", provider, providerId);
            return CommonResponse.failed(CustomError.ID_TOKEN_UNAUTHORIZED);
        }

        log.info("✅ id token 검증 완료");

        // 소셜 로그인 객체 생성 & 저장
        MemberSocial memberSocial = MemberSocial.builder()
                .provider(provider)
                .providerId(providerId)
                .build();

        memberSocialRepository.save(memberSocial);
        log.info("✅ 소셜 로그인 정보 저장 완료 - provider: {}, providerId: {}", provider, providerId);

        // 회원 객체 생성 & 저장
//        log.info("‼\uFE0F 프로필 사진 기본 이미지 경로 변경 필요");
//        Member member = Member.builder()
//                .id(requestDto.id())
//                .nickname(requestDto.nickname())
//                .profileImg(DEFAULT_PROFILE_IMG)
//                .birth(requestDto.birth())
//                .gender(new CustomGender(requestDto.gender()))
//                .memberSocial(memberSocial)
//                .build();
        log.info("‼️ 프로필 사진 기본 이미지 경로 변경 필요");

        // gender 값이 null이거나 빈 문자열이면 기본값 설정
        String genderValue = (requestDto.gender() == null || requestDto.gender().isEmpty()) ? "other" : requestDto.gender();

        Member member = Member.builder()
                .id(requestDto.id())
                .nickname(requestDto.nickname())
                .profileImg(DEFAULT_PROFILE_IMG)
                .birth(requestDto.birth())
                .gender(new CustomGender(genderValue)) // Null 체크 후 적용
                .memberSocial(memberSocial)
                .build();

        memberRepository.save(member);
        log.info("✅ 회원 정보 저장 완료 - id: {}, nickname: {}", member.getId(), member.getNickname());

        // 약관 동의 객체 생성 & 저장
        TermsAgreement termsAgreement = TermsAgreement.builder()
                .member(member)
                .isAgree(requestDto.isAgree())
                .build();

        termsAgreementRepository.save(termsAgreement);
        log.info("✅ 약관 동의 정보 저장 완료 - isAgree: {}", requestDto.isAgree());

        // 알림 설정 동의 객체 생성 & 저장 (기본값: 모든 알림 ON)
        PushSetting pushSetting = PushSetting.builder()
                .member(member)
                .build();

        pushSettingRepository.save(pushSetting);
        log.info("✅ 알림 설정 저장 완료 - memberId: {}", member.getId());

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
        log.info("✅ 토큰 생성 완료 - accessToken: {}, refreshToken: {}", accessToken, refreshToken);

        String name = member.getNickname();
        String id = member.getId();

        log.info("🟢 createAccount 완료 - memberId: {}, name: {}", id, name);
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
    전화번호 인증코드 전송
     */
    public ResponseEntity<CommonResponse<String>> sendSmsCode(String receiver) throws CoolsmsException {
        try {
            // 6자리 인증코드 생성
            String code = String.format("%06d", new SecureRandom(), nextInt(1000000));

            // 생성자로 API key & secret 전달
            Message coolsms = new Message(apiKey, apiSecret);

            HashMap<String, String> params = new HashMap<String, String>();
            params.put("to", receiver);
            params.put("from", sender);
            params.put("type", "sms");
            params.put("text", "[핍잇] 본인확인 인증번호 [" + code + "]를 화면에 입력해주세요!");

            coolsms.send(params);

            return CommonResponse.ok(code);
        } catch (Exception e) {
//            throw new CoolsmsException("SMS 전송에 실패했습니다", e);
            return CommonResponse.exception(e);
        }
    }
}