package com.b02.peep_it.service;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.exception.CustomError;
import com.b02.peep_it.common.response.PagedResponse;
import com.b02.peep_it.common.s3.S3Utils;
import com.b02.peep_it.common.util.AuthUtils;
import com.b02.peep_it.common.util.TimeAgoUtils;
import com.b02.peep_it.domain.Member;
import com.b02.peep_it.domain.Peep;
import com.b02.peep_it.domain.PeepLocation;
import com.b02.peep_it.dto.CommonPeepDto;
import com.b02.peep_it.dto.peep.RequestPeepUploadDto;
import com.b02.peep_it.repository.PeepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PeepService {
    private final AuthUtils userInfo;
    private final S3Utils s3Utils;
    private final PeepRepository peepRepository;

    /*
    신규 핍 등록 (텍스트 + 이미지/영상)
     */
    public ResponseEntity<CommonResponse<CommonPeepDto>> createPeep(
            RequestPeepUploadDto requestDto, MultipartFile media) {
        try {
            // 1. S3에 파일 업로드 후 URL 받기
            String mediaUrl = s3Utils.uploadFile(media);

            // 2. 작성자 가져오기
            Member member = userInfo.getCurrentMember();

            // 3. 핍 객체 생성
            Peep peep = Peep.builder()
                    .legalDistrictCode(requestDto.legalDistrictCode())
                    .imageUrl(mediaUrl)
                    .content(requestDto.content())
                    .member(member)
                    .build();

            // 4. PeepLocation 객체 생성 및 추가
            PeepLocation peepLocation = PeepLocation.builder()
                    .peep(peep)
                    .latitude(requestDto.latitude())
                    .longitude(requestDto.longitude())
                    .postalCode(requestDto.postalCode())
                    .roadNameAddress(requestDto.roadNameAddress())
                    .roadNameCode(requestDto.roadNameCode())
                    .building(requestDto.building())
                    .build();

            peep.updatePeepLocation(peepLocation);

            // 5. 핍 객체 저장
            peepRepository.save(peep);

            // 6. response dto 생성
            CommonPeepDto responseDto = CommonPeepDto.builder()
                    .peepId(peep.getId())
                    .memberId(member.getId())
                    .legalDistrictCode(peep.getLegalDistrictCode())
                    .imageUrl(peep.getImageUrl())
                    .content(peep.getContent())
                    .isEdited(peep.getIsEdited())
                    .profileUrl(member.getProfileImg())
                    .uploadAt(TimeAgoUtils.getTimeAgo(peep.getCreatedAt()))
                    .stickerNum(Optional.ofNullable(peep.getPeepReStickerList()).map(l -> l.size()).orElse(0))
                    .chatNum(Optional.ofNullable(peep.getChatList()).map(l -> l.size()).orElse(0))
                    .build();

            // 7. response 반환
            return CommonResponse.created(responseDto);

        } catch (IOException e) {
            return CommonResponse.failed(CustomError.NEED_TO_CUSTOM);
//            throw new RuntimeException(e);
        }
    }

    /*
    핍 고유 ID로 개별 핍 상세 조회
     */
    public ResponseEntity<CommonResponse<CommonPeepDto>> getPeepById(Long peepId) {
        // 1. 핍 객체 조회
        Optional<Peep> optionalPeep = peepRepository.findById(peepId);
        if (optionalPeep.isEmpty()) {
            return CommonResponse.failed(CustomError.PEEP_NOT_FOUND);
        }

        Peep peep = optionalPeep.get();

        // 2. response dto 생성
        CommonPeepDto responseDto = CommonPeepDto.builder()
                .peepId(peep.getId())
                .memberId(peep.getMember().getId())
                .legalDistrictCode(peep.getLegalDistrictCode())
                .imageUrl(peep.getImageUrl())
                .content(peep.getContent())
                .isEdited(peep.getIsEdited())
                .profileUrl(peep.getMember().getProfileImg())
                .uploadAt(TimeAgoUtils.getTimeAgo(peep.getCreatedAt()))
                .stickerNum(Optional.ofNullable(peep.getPeepReStickerList()).map(l -> l.size()).orElse(0))
                .chatNum(Optional.ofNullable(peep.getChatList()).map(l -> l.size()).orElse(0))
                .build();

        // 3. response 반환
        return CommonResponse.created(responseDto);
    }

    /*
    사용자가 업로드한 핍 리스트 조회
     */
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getMyUploadPeepList(int page, int size) {
        // 1. 현재 로그인 사용자 ID 조회
        String memberId = userInfo.getCurrentMemberUid();

        // 2. 페이징 처리하여 Peep 리스트 조회
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")); // 최신순 정렬
        Page<Peep> peepPage = peepRepository.findAllByMember_Id(memberId, pageRequest);

        // 3. Peep 객체 리스트를 CommonPeepDto 리스트로 변환
        List<CommonPeepDto> responseDto = peepPage.getContent().stream().map(p -> CommonPeepDto.builder()
                .peepId(p.getId())
                .memberId(p.getMember().getId())
                .legalDistrictCode(p.getLegalDistrictCode())
                .imageUrl(p.getImageUrl())
                .content(p.getContent())
                .isEdited(p.getIsEdited())
                .profileUrl(p.getMember().getProfileImg())
                .uploadAt(TimeAgoUtils.getTimeAgo(p.getCreatedAt()))
                .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                .build()
        ).toList();

        // 4. PagedResponse 객체 생성
        PagedResponse<CommonPeepDto> pagedResponse = PagedResponse.create(
                responseDto,
                peepPage.getNumber(),
                peepPage.getSize(),
                peepPage.getTotalPages(),
                peepPage.getTotalElements()
        );

        // 5. response 반환
        return CommonResponse.created(pagedResponse);
    }
}
