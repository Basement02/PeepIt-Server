package com.b02.peep_it.service;

import com.b02.peep_it.common.exception.CustomError;
import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.util.AuthUtils;
import com.b02.peep_it.domain.Member;
import com.b02.peep_it.domain.TermsAgreement;
import com.b02.peep_it.dto.RequestPatchMemberDto;
import com.b02.peep_it.dto.member.RequestCommonMemberDto;
import com.b02.peep_it.dto.member.ResponseCommonMemberDto;
import com.b02.peep_it.repository.MemberRepository;
import com.b02.peep_it.repository.TermsAgreementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    private final AuthUtils userInfo;
    private final MemberRepository memberRepository;
    private final TermsAgreementRepository termsAgreementRepository;

    /*
    아이디로 사용자 정보 반환 (id, name, town, profile)
     */
    @Transactional
    public ResponseEntity<CommonResponse<ResponseCommonMemberDto>> getMemberDetail(String memberId) {
        // 아이디 조회
        Optional<Member> optionalMember = memberRepository.findById(memberId);

        // 부재 시 Exception
        if (optionalMember.isEmpty()) {
            return CommonResponse.failed(CustomError.MEMBER_NOT_FOUND); // 존재하지 않는 사용자입니다
        }

        Member member = optionalMember.get();

        // responseDto 구성 (id, role, name, gender, town, profile)
        ResponseCommonMemberDto responseDto = ResponseCommonMemberDto.builder()
                .role(member.getRole().getCode())
                .id(memberId)
                .name(member.getNickname())
                .gender(member.getGender().getValue())
                .town(member.getTown().getStateName())
                .profile(member.getProfileImg())
                .build();

        // return
        return CommonResponse.ok(responseDto);
    }

    /*
    현재 로그인한 사용자 정보 반환
     */
    @Transactional
    public ResponseEntity<CommonResponse<ResponseCommonMemberDto>> getMyDetail() {
        // 아이디 조회
        Member member = userInfo.getCurrentMember();

        // 부재 시 Exception
        if (member == null) {
            return CommonResponse.failed(CustomError.MEMBER_UNAUTHORIZED); // 유효하지 않은 계정입니다
        }

        // responseDto 구성 (id, role, name, gender, town, profile)
        ResponseCommonMemberDto responseDto = ResponseCommonMemberDto.builder()
                .role(member.getRole().getCode())
                .id(member.getId())
                .name(member.getNickname())
                .gender(member.getGender().getValue())
                .town(member.getTown().getStateName())
                .profile(member.getProfileImg())
                .build();

        // return
        return CommonResponse.ok(responseDto);
    }

    /*
    사용자 정보 수정
    - 닉네임
    - 성별
    - 생일
    - 마케팅 약관 동의
     */
    @Transactional
    public ResponseEntity<CommonResponse<ResponseCommonMemberDto>> patchMyDetail(RequestPatchMemberDto requestDto) throws Exception {
        // 사용자 조회
        Member member = userInfo.getCurrentMember();
        // 부재 시 Exception
        if (member == null) {
            return CommonResponse.failed(CustomError.MEMBER_UNAUTHORIZED); // 유효하지 않은 계정입니다
        }

        // 마케팅 약관 동의 설정 변경
        TermsAgreement termsAgreement = termsAgreementRepository.findById(member.getId()).orElseThrow(() ->
                new Exception("termsAgreement 정보가 존재하지 않습니다."));

        if (requestDto != null) {
            if (requestDto.isAgree()) {
                termsAgreement.setAgree();
            }
            else {
                termsAgreement.setDisAgree();
            }
        }

        // 사용자 정보 수정 (닉네임, 성별, 생일)
        member = member.withPatched(requestDto);

        ResponseCommonMemberDto responseDto = ResponseCommonMemberDto.builder()
                .role(member.getRole().getCode())
                .id(member.getId())
                .name(member.getNickname())
                .gender(member.getGender().getValue())
                .town(member.getTown().getStateName())
                .profile(member.getProfileImg())
                .isAgree(termsAgreement.getIsAgree())
                .build();

        return CommonResponse.ok(responseDto);
    }
}
