package com.b02.peep_it.service;

import com.b02.peep_it.common.exception.CustomError;
import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.util.AuthUtils;
import com.b02.peep_it.domain.Member;
import com.b02.peep_it.dto.member.CommonMemberDto;
import com.b02.peep_it.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    private final AuthUtils userInfo;
    private final MemberRepository memberRepository;

    /*
    아이디로 사용자 정보 반환 (id, name, town, profile)
     */
    public ResponseEntity<CommonResponse<CommonMemberDto>> getMemberDetail(String memberId) {
        // 아이디 조회
        Optional<Member> optionalMember = memberRepository.findById(memberId);

        // 부재 시 Exception
        if (optionalMember.isEmpty()) {
            return CommonResponse.failed(CustomError.MEMBER_NOT_FOUND); // 존재하지 않는 사용자입니다
        }

        Member member = optionalMember.get();

        // responseDto 구성 (id, role, name, town, profile)
        CommonMemberDto responseDto = CommonMemberDto.builder()
                .id(memberId)
                .name(member.getNickname())
                .town(member.getTown().getStateName())
                .profile(member.getProfileImg())
                .build();

        // return
        return CommonResponse.ok(responseDto);
    }

    /*
    현재 로그인한 사용자 정보 반환
     */
    public ResponseEntity<CommonResponse<CommonMemberDto>> getMyDetail() {
        // 아이디 조회
        Member member = userInfo.getCurrentMember();

        // 부재 시 Exception
        if (member == null) {
            return CommonResponse.failed(CustomError.MEMBER_UNAUTHORIZED); // 유효하지 않은 계정입니다
        }

        // responseDto 구성 (id, role, name, town, profile)
        CommonMemberDto responseDto = CommonMemberDto.builder()
                .id(member.getId())
                .name(member.getNickname())
                .town(member.getTown().getStateName())
                .profile(member.getProfileImg())
                .build();

        // return
        return CommonResponse.ok(responseDto);
    }
}
