package com.b02.peep_it.service;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.exception.CustomError;
import com.b02.peep_it.common.response.PagedResponse;
import com.b02.peep_it.common.s3.S3Utils;
import com.b02.peep_it.common.util.AuthUtils;
import com.b02.peep_it.common.util.TimeAgoUtils;
import com.b02.peep_it.domain.*;
import com.b02.peep_it.dto.peep.CommonPeepDto;
import com.b02.peep_it.dto.peep.RequestPeepUploadDto;
import com.b02.peep_it.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    private final TimeAgoUtils timeAgoUtils;
    private final PeepRepository peepRepository;
    private final PeepReStickerRepository peepReStickerRepository;
    private final ChatRepository chatRepository;
    private final TownRepository townRepository;
    private final StateRepository stateRepository;

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
                    .town(stateRepository.findByCode(requestDto.legalDistrictCode()))
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
                    .town(peep.getTown())
                    .legalDistrictCode(peep.getLegalDistrictCode())
                    .imageUrl(peep.getImageUrl())
                    .content(peep.getContent())
                    .isEdited(peep.getIsEdited())
                    .profileUrl(member.getProfileImg())
                    .isActive(timeAgoUtils.isActiveWithin24Hours(peep.getCreatedAt()))
                    .uploadAt(timeAgoUtils.getTimeAgo(peep.getCreatedAt()))
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
                .town(peep.getTown())
                .legalDistrictCode(peep.getLegalDistrictCode())
                .imageUrl(peep.getImageUrl())
                .content(peep.getContent())
                .isEdited(peep.getIsEdited())
                .profileUrl(peep.getMember().getProfileImg())
                .isActive(timeAgoUtils.isActiveWithin24Hours(peep.getCreatedAt()))
                .uploadAt(timeAgoUtils.getTimeAgo(peep.getCreatedAt()))
                .stickerNum(Optional.ofNullable(peep.getPeepReStickerList()).map(l -> l.size()).orElse(0))
                .chatNum(Optional.ofNullable(peep.getChatList()).map(l -> l.size()).orElse(0))
                .build();

        // 3. response 반환
        return CommonResponse.created(responseDto);
    }

    /*
    사용자가 업로드한 핍 리스트 조회
     */
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getUploadedPeepList(int page, int size) {
        // 1. 현재 로그인 사용자 ID 조회
        String memberId = userInfo.getCurrentMemberUid();

        // 2. 페이징 처리하여 Peep 리스트 조회
        // 최신순 정렬
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Peep> peepPage = peepRepository.findAllByMember_Id(memberId, pageRequest);

        // 3. Peep 객체를 CommonPeepDto로 변환 (Page.map() 사용)
        // Page.map()을 사용하면 JPA 내부적으로 최적화된 변환(내부적으로 PageImpl을 변환하여 Page<CommonPeepDto>를 바로 생성) 제공
        // stream().map().toList()를 사용하는 것보다 성능이 더 좋음
        Page<CommonPeepDto> responseDtoPage = peepPage.map(p -> CommonPeepDto.builder()
                .peepId(p.getId())
                .memberId(p.getMember().getId())
                .town(p.getTown())
                .legalDistrictCode(p.getLegalDistrictCode())
                .imageUrl(p.getImageUrl())
                .content(p.getContent())
                .isEdited(p.getIsEdited())
                .profileUrl(p.getMember().getProfileImg())
                .isActive(timeAgoUtils.isActiveWithin24Hours(p.getCreatedAt()))
                .uploadAt(timeAgoUtils.getTimeAgo(p.getCreatedAt()))
                .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                .build());

        // 4. PagedResponse 객체 생성
        PagedResponse<CommonPeepDto> pagedResponse = PagedResponse.create(
                responseDtoPage.getContent(),
                responseDtoPage.getNumber(),
                responseDtoPage.getSize(),
                responseDtoPage.getTotalPages(),
                responseDtoPage.getTotalElements()
        );

        // 5. response 반환
        return CommonResponse.ok(pagedResponse);
    }

    /*
    사용자가 반응한 핍 리스트 조회
     */
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getReactedPeepList(int page, int size) {
        // 1. 현재 로그인 사용자 ID 조회
        String memberId = userInfo.getCurrentMemberUid();

        // 2. 페이징 처리하여 `PeepReSticker`를 조회하면서 `peep`을 함께 가져오기
        // 최신순 정렬
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Peep> peepReStickerPage = peepReStickerRepository.findAllByMember_Id(memberId, pageRequest);

        // 3. `PeepReSticker`에서 `CommonPeepDto`로 직접 변환하여 새로운 Page 객체 생성 (Page.map() 사용)
        // Page.map()을 사용하면 JPA 내부적으로 최적화된 변환(내부적으로 PageImpl을 변환하여 Page<CommonPeepDto>를 바로 생성) 제공
        // stream().map().toList()를 사용하는 것보다 성능이 더 좋음
        Page<CommonPeepDto> responseDtoPage = peepReStickerPage.map(p -> {
            return CommonPeepDto.builder()
                    .peepId(p.getId())
                    .memberId(p.getMember().getId())
                    .town(p.getTown())
                    .legalDistrictCode(p.getLegalDistrictCode())
                    .imageUrl(p.getImageUrl())
                    .content(p.getContent())
                    .isEdited(p.getIsEdited())
                    .profileUrl(p.getMember().getProfileImg())
                    .isActive(timeAgoUtils.isActiveWithin24Hours(p.getCreatedAt()))
                    .uploadAt(timeAgoUtils.getTimeAgo(p.getCreatedAt()))
                    .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                    .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                    .build();
        });

        // 4. PagedResponse 객체 생성
        PagedResponse<CommonPeepDto> pagedResponse = PagedResponse.create(
                responseDtoPage.getContent(),
                responseDtoPage.getNumber(),
                responseDtoPage.getSize(),
                responseDtoPage.getTotalPages(),
                responseDtoPage.getTotalElements()
        );

        // 5. response 반환
        return CommonResponse.ok(pagedResponse);
    }

    /*
    사용자가 댓글 단 핍 리스트 조회
     */
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getChatPeepList(int page, int size) {
        // 1. 현재 로그인 사용자 ID 조회
        String memberId = userInfo.getCurrentMemberUid();

        // 2. 페이징 처리하여 `Chat`을 조회하면서 `peep`을 함께 가져오기
        // memberId로 Chat의 peep을 중복없이 조회
        // 최신순 정렬
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Peep> chatPage = chatRepository.findDistinctPeepsByMemberId(memberId, pageRequest);

        // 3. `PeepReSticker`에서 `CommonPeepDto`로 직접 변환하여 새로운 Page 객체 생성 (Page.map() 사용)
        // Page.map()을 사용하면 JPA 내부적으로 최적화된 변환(내부적으로 PageImpl을 변환하여 Page<CommonPeepDto>를 바로 생성) 제공
        // stream().map().toList()를 사용하는 것보다 성능이 더 좋음
        Page<CommonPeepDto> responseDtoPage = chatPage.map(p -> {
            return CommonPeepDto.builder()
                    .peepId(p.getId())
                    .memberId(p.getMember().getId())
                    .town(p.getTown())
                    .legalDistrictCode(p.getLegalDistrictCode())
                    .imageUrl(p.getImageUrl())
                    .content(p.getContent())
                    .isEdited(p.getIsEdited())
                    .profileUrl(p.getMember().getProfileImg())
                    .isActive(timeAgoUtils.isActiveWithin24Hours(p.getCreatedAt()))
                    .uploadAt(timeAgoUtils.getTimeAgo(p.getCreatedAt()))
                    .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                    .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                    .build();
        });

        // 4. PagedResponse 객체 생성
        PagedResponse<CommonPeepDto> pagedResponse = PagedResponse.create(
                responseDtoPage.getContent(),
                responseDtoPage.getNumber(),
                responseDtoPage.getSize(),
                responseDtoPage.getTotalPages(),
                responseDtoPage.getTotalElements()
        );

        // 5. response 반환
        return CommonResponse.ok(pagedResponse);
    }

    /*
    특정 사용자가 업로드한 핍 리스트 조회
     */
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getMemberPeepList(String memberId, int page, int size) {
        // 1. 페이징 처리하여 Peep 리스트 조회
        // 최신순 정렬
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Peep> peepPage = peepRepository.findAllByMember_Id(memberId, pageRequest);

        // 2. Peep 객체를 CommonPeepDto로 변환 (Page.map() 사용)
        // Page.map()을 사용하면 JPA 내부적으로 최적화된 변환(내부적으로 PageImpl을 변환하여 Page<CommonPeepDto>를 바로 생성) 제공
        // stream().map().toList()를 사용하는 것보다 성능이 더 좋음
        Page<CommonPeepDto> responseDtoPage = peepPage.map(p -> CommonPeepDto.builder()
                .peepId(p.getId())
                .memberId(p.getMember().getId())
                .town(p.getTown())
                .legalDistrictCode(p.getLegalDistrictCode())
                .imageUrl(p.getImageUrl())
                .content(p.getContent())
                .isEdited(p.getIsEdited())
                .profileUrl(p.getMember().getProfileImg())
                .isActive(timeAgoUtils.isActiveWithin24Hours(p.getCreatedAt()))
                .uploadAt(timeAgoUtils.getTimeAgo(p.getCreatedAt()))
                .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                .build());

        // 3. PagedResponse 객체 생성
        PagedResponse<CommonPeepDto> pagedResponse = PagedResponse.create(
                responseDtoPage.getContent(),
                responseDtoPage.getNumber(),
                responseDtoPage.getSize(),
                responseDtoPage.getTotalPages(),
                responseDtoPage.getTotalElements()
        );

        // 4. response 반환
        return CommonResponse.ok(pagedResponse);
    }

    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getActivePeepList(int page, int size) {
        // 1. 현재 로그인 사용자 ID 조회
        String memberId = userInfo.getCurrentMemberUid();

        // 2. 페이징 처리하여 `Chat`을 조회하면서 `peep`을 함께 가져오기
        // memberId로 Chat의 peep을 중복없이 조회
        // 최신순 정렬
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Peep> chatPage = chatRepository.findDistinctPeepsByMemberId(memberId, pageRequest);

        // 3. `PeepReSticker`에서 `CommonPeepDto`로 직접 변환하여 새로운 Page 객체 생성 (Page.map() 사용)
        // Page.map()을 사용하면 JPA 내부적으로 최적화된 변환(내부적으로 PageImpl을 변환하여 Page<CommonPeepDto>를 바로 생성) 제공
        // stream().map().toList()를 사용하는 것보다 성능이 더 좋음
        Page<CommonPeepDto> responseDtoPage = chatPage.map(p -> {
            return CommonPeepDto.builder()
                    .peepId(p.getId())
                    .memberId(p.getMember().getId())
                    .town(p.getTown())
                    .legalDistrictCode(p.getLegalDistrictCode())
                    .imageUrl(p.getImageUrl())
                    .content(p.getContent())
                    .isEdited(p.getIsEdited())
                    .profileUrl(p.getMember().getProfileImg())
                    .isActive(timeAgoUtils.isActiveWithin24Hours(p.getCreatedAt()))
                    .uploadAt(timeAgoUtils.getTimeAgo(p.getCreatedAt()))
                    .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                    .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                    .build();
        });

        // 4. `isActive == false`인 항목을 최종 응답에서 제거
        List<CommonPeepDto> filteredDtoList = responseDtoPage.getContent().stream()
                .filter(CommonPeepDto::isActive)
                .toList();

        // 5. 새로운 `Page` 객체 생성 (filteredDtoList 크기에 맞게 `PageImpl` 사용)
        Page<CommonPeepDto> filteredPage = new PageImpl<>(filteredDtoList, pageRequest, filteredDtoList.size());

        // 6. PagedResponse 객체 생성
        PagedResponse<CommonPeepDto> pagedResponse = PagedResponse.create(
                filteredPage.getContent(),
                filteredPage.getNumber(),
                filteredPage.getSize(),
                filteredPage.getTotalPages(),
                filteredPage.getTotalElements()
        );

        // 6. response 반환
        return CommonResponse.ok(pagedResponse);
    }
}
