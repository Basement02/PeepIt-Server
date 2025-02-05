package com.b02.peep_it.service;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.exception.CustomError;
import com.b02.peep_it.domain.Member;
import com.b02.peep_it.domain.MemberSocial;
import com.b02.peep_it.dto.RequestSignUpDto;
import com.b02.peep_it.dto.RequestSocialLoginDto;
import com.b02.peep_it.dto.ResponseSocialLoginDto;
import com.b02.peep_it.dto.member.CommonMemberDto;
import com.b02.peep_it.repository.MemberRepository;
import com.b02.peep_it.repository.MemberSocialRepository;
import com.b02.peep_it.common.util.JwtUtils;
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
    private final MemberRepository memberRepository;
    private final MemberSocialRepository memberSocialRepository;

    /*
    register token 발급
    - provider에 따라 idtoken 유효성 검증
    - 기존 회원 정보와 중복 확인 (idtoken)
    - provider, providerId(idtoken)으로 register token 생성
    - response body의 data-registerToken에 담아 반환

    error code: message
    - 40101: 유효하지 않은 소셜 계정입니다
    - 50000: 서버 내부 오류가 발생했습니다
     */
    public ResponseEntity<CommonResponse<ResponseSocialLoginDto>> getRegisterToken(RequestSocialLoginDto requstDto) {
        Boolean isMember = Boolean.FALSE;
        String registerToken = "";
        String accessToken = "";
        String refreshToken = "";
        String name = "";
        String id = "";

        // idtoken 유효성 검증

        // idtoken에서 고유 id 추출
        String providerId = ""; // idToken으로 얻기!!!!!!!!!!!!!!!!!

        // 기존 회원과 provider 고유 id 중복 확인
        Optional<MemberSocial> memberSocial = memberSocialRepository.findByProviderAndProviderId(requstDto.provider().getCode(), providerId);

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
            registerToken = jwtUtils.createRegisterToken(requstDto.provider().getCode(), providerId);
        }
        return CommonResponse.created(ResponseSocialLoginDto.builder()
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
}
