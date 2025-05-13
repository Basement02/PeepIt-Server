package com.b02.peep_it.service;

import com.b02.peep_it.common.exception.UnauthorizedException;
import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.exception.CustomError;
import com.b02.peep_it.common.util.CustomUserDetails;
import com.b02.peep_it.common.util.AuthUtils;
import com.b02.peep_it.domain.*;
import com.b02.peep_it.domain.constant.CustomProvider;
import com.b02.peep_it.domain.constant.Role;
import com.b02.peep_it.dto.member.RequestCommonMemberDto;
import com.b02.peep_it.dto.RequestSocialLoginDto;
import com.b02.peep_it.dto.ResponseLoginDto;
import com.b02.peep_it.dto.SmsAuthDto;
import com.b02.peep_it.dto.member.ResponseCommonMemberDto;
import com.b02.peep_it.repository.*;
import com.b02.peep_it.common.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.math.RandomUtils.nextInt;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final TownRepository townRepository;
    @Value("${coolsms.api.key}")
    String apiKey;
    @Value("${coolsms.api.secret}")
    String apiSecret;
    @Value("${coolsms.api.number}")
    String sender;
    private final JwtUtils jwtUtils;
    private final AuthUtils userInfo;
    private final MemberRepository memberRepository;
    private final MemberSocialRepository memberSocialRepository;
    private final TermsAgreementRepository termsAgreementRepository;
    private final PushSettingRepository pushSettingRepository;
    private final StateRepository stateRepository;

    @Autowired
    private ObjectMapper redisObjectMapper;
    @Autowired
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DEFAULT_PROFILE_IMG = "추후수정필요 프로필 이미지 고정값";
    private static final String PREFIX = "SMS_AUTH:";
    private static final int MAX_TRY = 3;
    private static final Duration EXPIRE_TIME = Duration.ofMinutes(5);


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
        String providerId = jwtUtils.getSocialUid(provider, requestDto.idToken());

        // 기존 회원과 provider 고유 id 중복 확인
        Optional<MemberSocial> memberSocial = memberSocialRepository.findByProviderAndProviderId(provider, providerId);

        // 기존 회원은 access/refresh token 발급 (로그인)
        if (memberSocial.isPresent()) {
            isMember = Boolean.TRUE;
            Optional<Member> memberOptional = memberRepository.findByMemberSocial(memberSocial.get());

            // member가 존재하지 않으면 예외 발생 (메시지 포함)
            Member member = memberOptional.orElseThrow(() ->
                    new Exception("Member 정보가 존재하지 않습니다. socialId: " + memberSocial.get().getProviderId())
            );

            ResponseCommonMemberDto responseCommonMemberDto = ResponseCommonMemberDto.builder()
                    .id(member.getId())
                    .role(member.getRole().toString())
                    .name(member.getNickname())
                    .build();

            accessToken = jwtUtils.createAccessToken(responseCommonMemberDto);
            refreshToken = jwtUtils.createRefreshToken(responseCommonMemberDto);
            name = member.getNickname();
            id = member.getId();
        }

        // 신규 회원은 register token 발급 (가입 대기)
        else {

            // idtoken 유효성 검증
            if (!jwtUtils.validateIdToken(provider, providerId)) {
                log.warn("유효하지 않은 id token - provider: {}, providerId: {}", provider, providerId);
                return CommonResponse.failed(CustomError.TOKEN_UNAUTHORIZED);
            }

            log.info("id token 검증 완료");

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
    public ResponseEntity<CommonResponse<ResponseLoginDto>> createAccount(RequestCommonMemberDto requestDto) throws IOException, InterruptedException {
        log.info("createAccount 시작 - requestDto: {}", requestDto);

        // 레지스터 토큰에서 사용자 정보 추출
        CustomUserDetails userDetails = userInfo.getPrincipal();
        log.info("사용자 정보 추출 완료 - provider: {}, providerId: {}", userDetails.getProvider(), userDetails.getProviderId());

        CustomProvider provider = CustomProvider.valueOf(userDetails.getProvider());
        String providerId = userDetails.getProviderId();

        // idtoken 유효성 검증
        if (!jwtUtils.validateIdToken(provider, providerId)) {
            log.warn("유효하지 않은 id token - provider: {}, providerId: {}", provider, providerId);
            return CommonResponse.failed(CustomError.TOKEN_UNAUTHORIZED);
        }

        log.info("id token 검증 완료");

        // 소셜 로그인 객체 생성 & 저장
        MemberSocial memberSocial = MemberSocial.builder()
                .provider(provider)
                .providerId(providerId)
                .build();

        MemberSocial mergedMemberSocial = memberSocialRepository.save(memberSocial);
        log.info("소셜 로그인 정보 저장 완료 - provider: {}, providerId: {}", provider, providerId);

        if (requestDto.id() == null || requestDto.id().isEmpty()) {
            log.info("id가 비어있음");
            return CommonResponse.failed(CustomError.NEED_TO_CUSTOM);
        }
        if (requestDto.nickname() == null || requestDto.nickname().isEmpty()) {
            log.info("nickname이 비어있음");
            return CommonResponse.failed(CustomError.NEED_TO_CUSTOM);
        }

        log.info("프로필 사진 기본 이미지 경로 변경 필요");

        // gender 값이 null이거나 빈 문자열이면 기본값 설정
        String genderValue = (requestDto.gender() == null || requestDto.gender().isEmpty()) ? "other" : requestDto.gender();

        Member member = Member.builder()
                .id(requestDto.id())
                .nickname(requestDto.nickname())
                .profileImg(DEFAULT_PROFILE_IMG)
                .birth(requestDto.birth())
                .gender(new CustomGender(genderValue))
                .build();

        member.setMemberSocial(mergedMemberSocial);

        Member mergedMember = memberRepository.save(member);
        log.info("회원 정보 저장 완료 - id: {}, nickname: {}", mergedMember.getId(), mergedMember.getNickname());

        // 약관 동의 객체 생성 & 저장
        TermsAgreement termsAgreement = TermsAgreement.builder()
                .member(mergedMember)
                .isAgree(requestDto.isAgree())
                .build();

        termsAgreementRepository.save(termsAgreement);
        log.info("약관 동의 정보 저장 완료 - isAgree: {}", requestDto.isAgree());

        // 알림 설정 동의 객체 생성 & 저장 (기본값: 모든 알림 ON)
        PushSetting pushSetting = PushSetting.builder()
                .member(mergedMember)
                .build();

        pushSettingRepository.save(pushSetting);
        log.info("알림 설정 저장 완료 - memberId: {}", mergedMember.getId());

        // 동네 객체 생성 & 저장 (기본값: random)
        Town town = Town.builder()
                .member(mergedMember)
//                .state(getRandomState())
                .state(null)
                .build();
        townRepository.save(town);

        // 로그인
        Boolean isMember = Boolean.TRUE;
        String registerToken = "";
        ResponseCommonMemberDto responseCommonMemberDto = ResponseCommonMemberDto.builder()
                .id(mergedMember.getId())
                .role(mergedMember.getRole().toString())
                .name(mergedMember.getNickname())
                .build();

        String accessToken = jwtUtils.createAccessToken(responseCommonMemberDto);
        String refreshToken = jwtUtils.createRefreshToken(responseCommonMemberDto);
        log.info("토큰 생성 완료 - accessToken: {}, refreshToken: {}", accessToken, refreshToken);

        String name = mergedMember.getNickname();
        String id = mergedMember.getId();

        log.info("createAccount 완료 - memberId: {}, name: {}", id, name);
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
    @Transactional
    public ResponseEntity<CommonResponse<String>> sendSmsCode(String receiver) throws CoolsmsException {
        try {
            // 6자리 인증코드 생성
            int codeNum = new SecureRandom().nextInt(1000000);
            String code = String.format("%06d", codeNum);

            log.info("생성된 인증 코드: {}", code);
            log.info("수신자 번호: {}", receiver);

            // 생성자로 API key & secret 전달
            Message coolsms = new Message(apiKey, apiSecret);

            HashMap<String, String> params = new HashMap<String, String>();
            params.put("to", receiver);
            params.put("from", sender);
            params.put("type", "sms");
            params.put("text", "[핍잇] 본인확인 인증번호 [" + code + "]를 화면에 입력해주세요!");

            log.info("CoolSMS params: {}", params);
            log.info("API Key: {}, Secret: {}", apiKey, apiSecret);
            log.info("Sender 번호: {}", sender);

            coolsms.send(params);

            // Redis에 저장
            SmsAuthDto redisSMS = new SmsAuthDto(code, 0, LocalDateTime.now());
            redisTemplate.opsForValue().set(PREFIX + receiver, redisSMS, EXPIRE_TIME);

            return CommonResponse.ok(null);
        }
        catch (CoolsmsException e) {
            log.error("CoolSMS 전송 실패: {} (code: {})", e.getMessage(), e.getCode());
            throw new CoolsmsException("SMS 전송에 실패했습니다: 쿨시스의 문제", 50000);
        }
        catch (Exception e) {
            log.error("!!!!!!!!!!!!SMS 전송 예외 발생 - message: {}", e.getMessage(), e);
            throw new CoolsmsException("SMS 전송에 실패했습니다", 50000);
//            return CommonResponse.exception(e);
        }
    }

    /*
    인증번호 검증
     */
    @Transactional
    public ResponseEntity<CommonResponse<ResponseCommonMemberDto>> verifySmsCode(String receiver, String inputCode) throws CoolsmsException {
        try {
            // 사용자 조회
            Member member = userInfo.getCurrentMember();
            // 부재 시 Exception
            if (member == null) {
                return CommonResponse.failed(CustomError.MEMBER_UNAUTHORIZED); // 유효하지 않은 계정입니다
            }

            String key = PREFIX + receiver;
//            SmsAuthDto saved = (SmsAuthDto) redisTemplate.opsForValue().get(key);
            // 명시적으로 역직렬화
            Object raw = redisTemplate.opsForValue().get(key);
            SmsAuthDto saved = redisObjectMapper.convertValue(raw, SmsAuthDto.class);

            if (saved == null) {
                return CommonResponse.failed(CustomError.SMS_EXPIRED);
            }

            if (saved.tryCount() >= MAX_TRY) {
                return CommonResponse.failed(CustomError.OVER_MAX_TRY);
            }

            if (!saved.code().equals(inputCode)) {
                // tryCount 증가 후 재저장
                SmsAuthDto updated = SmsAuthDto.builder()
                        .code(saved.code())
                        .tryCount(saved.tryCount() + 1)
                        .requestedAt(saved.requestedAt())
                        .build();
                redisTemplate.opsForValue().set(key, updated, EXPIRE_TIME); // TTL 유지
                return CommonResponse.failed(CustomError.WRONG_SMS);
            }

            // 성공 (redis key 삭제 & 사용자 role 변경(UNCERT -> CERT))
            redisTemplate.delete(key);
            member.certificate(Role.CERT);

            TermsAgreement termsAgreement = termsAgreementRepository.findById(member.getId()).orElseThrow(() ->
                    new Exception("termsAgreement 정보가 존재하지 않습니다.")
            );

            State state = member.getTown() != null ? member.getTown().getState() : null;
            String stateName = state != null ? state.getName() : null;
            String lecalCode = state != null ? state.getCode() : null;

//            return CommonResponse.ok(null);
            ResponseCommonMemberDto responseDto = ResponseCommonMemberDto.builder()
                    .role(member.getRole().getCode())
                    .id(member.getId())
                    .name(member.getNickname())
                    .gender(member.getGender().getValue())
                    .town(stateName)
                    .legalCode(lecalCode)
                    .profile(member.getProfileImg())
                    .isAgree(termsAgreement.getIsAgree())
                    .build();

            return CommonResponse.ok(responseDto);
        } catch (Exception e) {
            return CommonResponse.exception(e);
        }
    }

    /*
    랜덤 State
     */
    public State getRandomState() {
        List<State> allStates = stateRepository.findAll();
        if (allStates.isEmpty()) throw new IllegalStateException("State 테이블이 비어 있습니다.");
        return allStates.get(new Random().nextInt(allStates.size()));
    }

    /*
    로그아웃
     */
    public ResponseEntity<CommonResponse<Object>> logout(String bearerToken) {
        log.info("[로그아웃 요청] Authorization 헤더: {}", bearerToken);

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            log.info("[로그아웃 처리] Bearer 제거된 access token: {}", token);

            if (!jwtUtils.validateAccessToken(token)) {
                log.warn("[로그아웃 실패] 유효하지 않은 토큰");
                return CommonResponse.failed(CustomError.TOKEN_UNAUTHORIZED);
            }

            Claims claims = jwtUtils.getClaims(token);
            long expiration = claims.getExpiration().getTime() - System.currentTimeMillis();
            log.info("[로그아웃 처리] Token 남은 유효 시간(ms): {}", expiration);

            // Redis에 블랙리스트 등록
            redisTemplate.opsForValue().set(
                    "blacklist:" + token,
                    "logout",
                    expiration,
                    TimeUnit.MILLISECONDS
            );
            log.info("[Redis 등록 완료] 블랙리스트 키: blacklist:{}, TTL: {}ms", token, expiration);

            // Refresh Token도 제거
            String uid = claims.getSubject();
            redisTemplate.delete("refresh:" + uid);
            log.info("[Refresh 삭제 완료] Key: refresh:{}", uid);

            return CommonResponse.ok(null);
        }

        log.warn("[로그아웃 실패] Authorization 헤더 형식이 잘못됨");
        return CommonResponse.failed(CustomError.TOKEN_UNAUTHORIZED);
    }

}