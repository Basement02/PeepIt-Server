package com.b02.peep_it.service;

import com.b02.peep_it.common.exception.CustomError;
import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.util.AuthUtils;
import com.b02.peep_it.common.util.S3Utils;
import com.b02.peep_it.domain.Member;
import com.b02.peep_it.domain.MemberBlock;
import com.b02.peep_it.domain.State;
import com.b02.peep_it.domain.TermsAgreement;
import com.b02.peep_it.dto.RequestPatchMemberDto;
import com.b02.peep_it.dto.RequestPatchProfileImgDto;
import com.b02.peep_it.dto.member.ResponseCommonMemberDto;
import com.b02.peep_it.repository.MemberBlockRepository;
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
    private final S3Utils s3Utils;
    private final MemberRepository memberRepository;
    private final TermsAgreementRepository termsAgreementRepository;
    private final MemberBlockRepository memberBlockRepository;

    /*
    아이디로 사용자 정보 반환 (id, name, town, gender, profile, isAgree)
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
        Optional<TermsAgreement> optionalTermsAgreement = termsAgreementRepository.findById(memberId);
        TermsAgreement termsAgreement = optionalTermsAgreement.orElseGet(() -> TermsAgreement.builder()
                .member(member)
                .isAgree(false)
                .build());

        log.info("termsAgrreement: {}", termsAgreement.getIsAgree());

        if (termsAgreement.getIsAgree() == null) {
            termsAgreement.setDisAgree();
        }
        termsAgreementRepository.save(termsAgreement);

        log.info("termsAgrreement: {}", termsAgreement.getIsAgree());

        Boolean isBlocked = false;

        // 차단한 사용자인지 확인
        Optional<MemberBlock> optionalMemberBlock = memberBlockRepository.findByBlockerId(member.getId());
        if(optionalMemberBlock.isPresent() && optionalMemberBlock.get().getBlockedId().getId().equals(memberId)) {
            isBlocked = true;
        }

        State state = member.getTown() != null ? member.getTown().getState() : null;
        String stateName = state != null ? state.getName() : null;

        // responseDto 구성 (id, role, name, gender, town, profile)
        ResponseCommonMemberDto responseDto = ResponseCommonMemberDto.builder()
                .role(member.getRole().getCode())
                .id(memberId)
                .name(member.getNickname())
                .gender(member.getGender().getValue())
                .town(stateName)
                .legalCode(member.getTown().getState().getCode())
                .profile(member.getProfileImg())
                .isAgree(termsAgreement.getIsAgree())
                .isBlocked(isBlocked)
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

        Optional<TermsAgreement> optionalTermsAgreement = termsAgreementRepository.findById(member.getId());
        TermsAgreement termsAgreement;

        termsAgreement = optionalTermsAgreement.orElseGet(() -> TermsAgreement.builder()
                .member(member)
                .isAgree(false)
                .build());

        log.info("termsAgrreement: {}", termsAgreement.getIsAgree());

        if (termsAgreement.getIsAgree() == null) {
            termsAgreement.setDisAgree();
        }
        termsAgreementRepository.save(termsAgreement);

        log.info("termsAgrreement: {}", termsAgreement.getIsAgree());

        State state = member.getTown() != null ? member.getTown().getState() : null;
        String stateName = state != null ? state.getName() : null;


        // responseDto 구성 (id, role, name, gender, town, profile)
        ResponseCommonMemberDto responseDto = ResponseCommonMemberDto.builder()
                .role(member.getRole().getCode())
                .id(member.getId())
                .name(member.getNickname())
                .gender(member.getGender().getValue())
                .town(stateName)
                .legalCode(member.getTown().getState().getCode())
                .profile(member.getProfileImg())
                .isAgree(termsAgreement.getIsAgree())
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

        State state = member.getTown() != null ? member.getTown().getState() : null;
        String stateName = state != null ? state.getName() : null;

        ResponseCommonMemberDto responseDto = ResponseCommonMemberDto.builder()
                .role(member.getRole().getCode())
                .id(member.getId())
                .name(member.getNickname())
                .gender(member.getGender().getValue())
                .town(stateName)
                .profile(member.getProfileImg())
                .isAgree(termsAgreement.getIsAgree())
                .build();

        return CommonResponse.ok(responseDto);
    }

    /*
    프로필 이미지 변경
     */
    @Transactional
    public ResponseEntity<CommonResponse<ResponseCommonMemberDto>> patchMyProfileImg(RequestPatchProfileImgDto requestDto) throws Exception {
        // 사용자 조회
        Member member = userInfo.getCurrentMember();
        // 부재 시 Exception
        if (member == null) {
            return CommonResponse.failed(CustomError.MEMBER_UNAUTHORIZED); // 유효하지 않은 계정입니다
        }

        // 기존 이미지 S3에서 삭제 (이미지 URL → key 추출)
        String oldUrl = member.getProfileImg();
        if (oldUrl != null) {
            String oldKey = s3Utils.extractKeyFromUrl(oldUrl);
            s3Utils.deleteFile(oldKey);
        }

        // 새 이미지 업로드
        String newUrl = s3Utils.uploadFile(requestDto.profileImg(), "profile/");

        // Member 엔티티에 새 URL 적용
        member.updateProfileUrl(newUrl); // this.profileImg = newUrl;

        TermsAgreement termsAgreement = termsAgreementRepository.findById(member.getId()).orElseThrow(() ->
                new Exception("termsAgreement 정보가 존재하지 않습니다."));

        State state = member.getTown() != null ? member.getTown().getState() : null;
        String stateName = state != null ? state.getName() : null;

        ResponseCommonMemberDto responseDto = ResponseCommonMemberDto.builder()
                .role(member.getRole().getCode())
                .id(member.getId())
                .name(member.getNickname())
                .gender(member.getGender().getValue())
                .town(stateName)
                .profile(member.getProfileImg())
                .isAgree(termsAgreement.getIsAgree())
                .build();

        return CommonResponse.ok(responseDto);
    }

    public ResponseEntity<CommonResponse<Object>> blockMember(String memberId) throws Exception {
        // 로그인한 사용자 조회
        Member blocker = userInfo.getCurrentMember();

        // 차단할 사용자 조회
        Optional<Member> optionalBlocked = memberRepository.findById(memberId);
        if (optionalBlocked.isEmpty()) {
            throw new Exception("차단 멤버 ID가 유효하지 않습니다.");
        }

        Member blocked = optionalBlocked.get();

        if(blocker.getId().equals(blocked.getId())) {
            throw new Exception("차단 멤버 ID와 차단할 멤버 ID가 동일합니다.");
        }

        // MemberBlock 테이블에 객체 추가
        MemberBlock memberBlock = MemberBlock.builder()
                .blockerId(blocker)
                .blockedId(blocked)
                .build();

        memberBlockRepository.save(memberBlock);

        return CommonResponse.ok(null);
    }
}
