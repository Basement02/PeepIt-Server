package com.b02.peep_it.service;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.exception.CustomError;
import com.b02.peep_it.common.response.PagedResponse;
import com.b02.peep_it.common.util.S3Utils;
import com.b02.peep_it.common.util.AuthUtils;
import com.b02.peep_it.common.util.TimeAgoUtils;
import com.b02.peep_it.domain.*;
import com.b02.peep_it.dto.peep.CommonPeepDto;
import com.b02.peep_it.dto.peep.RequestPeepUploadDto;
import com.b02.peep_it.dto.peep.ResponsePeepsByTownDto;
import com.b02.peep_it.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PeepService {
    private final AuthUtils userInfo;
    private final S3Utils s3Utils;
    private final TimeAgoUtils timeAgoUtils;
    private final PeepRepository peepRepository;
    private final PeepLocationRepository peepLocationRepository;
    private final PeepReStickerRepository peepReStickerRepository;
    private final ChatRepository chatRepository;
    private final StateRepository stateRepository;
    private final TownRepository townRepository;
    private final ChatService chatService;

    /*
    신규 핍 등록 (텍스트 + 이미지/영상)
     */
    public ResponseEntity<CommonResponse<CommonPeepDto>> createPeep(
            RequestPeepUploadDto requestDto, MultipartFile media) {
        try {
            // 1. S3에 파일 업로드 후 URL 받기
            String mediaUrl = s3Utils.uploadFile(media, "/peep");

            // 2. 작성자 & 동네 가져오기
            Member member = userInfo.getCurrentMember();
            Optional<State> townName = stateRepository.findByCode(requestDto.legalDistrictCode());
            if (townName.isEmpty()) {
                return CommonResponse.failed(CustomError.TOWN_NOT_FOUND);
            }
            State town = townName.get();

            // 3. 핍 객체 생성
            Peep peep = Peep.builder()
                    .code(town)
                    .town(town.getName())
                    .imageUrl(mediaUrl)
                    .content(requestDto.content())
                    .member(member)
                    .isVideo(requestDto.isVideo())
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
            peepLocationRepository.save(peepLocation);

            // 5. 핍 객체 저장
            peepRepository.save(peep);

            // 6. response dto 생성
            CommonPeepDto responseDto = CommonPeepDto.builder()
                    .peepId(peep.getId())
                    .memberId(member.getId())
                    .town(peep.getTown())
                    .longitude(peepLocation.getLongitude())
                    .latitude(peepLocation.getLatitude())
                    .building(peepLocation.getBuilding())
                    .imageUrl(peep.getImageUrl())
                    .content(peep.getContent())
                    .isEdited(peep.getIsEdited())
                    .profileUrl(member.getProfileImg())
                    .isActive(timeAgoUtils.isActiveWithin24Hours(peep.getCreatedAt()))
                    .uploadAt(timeAgoUtils.getTimeAgo(peep.getCreatedAt()))
                    .stickerNum(Optional.ofNullable(peep.getPeepReStickerList()).map(l -> l.size()).orElse(0))
                    .chatNum(Optional.ofNullable(peep.getChatList()).map(l -> l.size()).orElse(0))
                    .popularity((double) 1)
                    .isVideo(peep.getIsVideo())
                    .build();

            // 7. 채팅방 생성 및 response 반환
            chatService.createRoom(responseDto.peepId());
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
                .longitude(peep.getPeepLocation().getLongitude())
                .latitude(peep.getPeepLocation().getLatitude())
                .building(peep.getPeepLocation().getBuilding())
                .imageUrl(peep.getImageUrl())
                .content(peep.getContent())
                .isEdited(peep.getIsEdited())
                .profileUrl(peep.getMember().getProfileImg())
                .isActive(timeAgoUtils.isActiveWithin24Hours(peep.getCreatedAt()))
                .uploadAt(timeAgoUtils.getTimeAgo(peep.getCreatedAt()))
                .stickerNum(Optional.ofNullable(peep.getPeepReStickerList()).map(l -> l.size()).orElse(0))
                .chatNum(Optional.ofNullable(peep.getChatList()).map(l -> l.size()).orElse(0))
                .popularity(peep.calculatePopularityScore())
                .isVideo(peep.getIsVideo())
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
                .longitude(p.getPeepLocation().getLongitude())
                .latitude(p.getPeepLocation().getLatitude())
                .building(p.getPeepLocation().getBuilding())
                .imageUrl(p.getImageUrl())
                .content(p.getContent())
                .isEdited(p.getIsEdited())
                .profileUrl(p.getMember().getProfileImg())
                .isActive(timeAgoUtils.isActiveWithin24Hours(p.getCreatedAt()))
                .uploadAt(timeAgoUtils.getTimeAgo(p.getCreatedAt()))
                .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                .popularity(p.calculatePopularityScore())
                .isVideo(p.getIsVideo())
                .build());

        if (responseDtoPage.isEmpty()) {
            return CommonResponse.failed(CustomError.PEEP_NOT_FOUND);
        }

        // 4. PagedResponse 객체 생성
        PagedResponse<CommonPeepDto> pagedResponse = PagedResponse.create(
                responseDtoPage.getContent(),
                responseDtoPage.getNumber(),
                responseDtoPage.getSize(),
                responseDtoPage.getTotalPages(),
                responseDtoPage.getTotalElements()
        );

        log.info("[📦 DB 원본 Page] page={}, size={}, totalElements={}, totalPages={}, hasNext={}",
                peepPage.getNumber(), peepPage.getSize(),
                peepPage.getTotalElements(), peepPage.getTotalPages(),
                peepPage.hasNext());

        log.info("[📦 변환된 DTO Page] page={}, size={}, totalPages={}, totalElements={}, hasNext={}",
                responseDtoPage.getNumber(), responseDtoPage.getSize(),
                responseDtoPage.getTotalPages(), responseDtoPage.getTotalElements(),
                responseDtoPage.hasNext());

        log.info("[📄 페이징 정보] Total elements: {}", responseDtoPage.getTotalElements());
        log.info("[📄 페이징 정보] Total pages   : {}", responseDtoPage.getTotalPages());
        log.info("[📄 페이징 정보] Current page  : {}", responseDtoPage.getNumber());
        log.info("[📄 페이징 정보] Has next      : {}", responseDtoPage.hasNext());
        log.info("[📄 페이징 정보] Is last       : {}", responseDtoPage.isLast());

        // 5. response 반환
        return CommonResponse.ok(pagedResponse);
    }

    /*
    나의 반응한/댓글 단 핍 리스트 조회
     */
    public ResponseEntity<CommonResponse<ResponsePeepsByTownDto>> getActionPeepList() {
        // 현재 로그인 사용자 ID 조회
        String memberId = userInfo.getCurrentMemberUid();
        // 반응한 핍
        List<Peep> reactedPeeps = peepReStickerRepository.findAllByMember_Id(memberId)
                .stream().map(PeepReSticker::getPeep).toList();

        // 댓글단 핍
        List<Peep> cattedPeeps = chatRepository.findDistinctPeepsByMemberId(memberId)
                .stream().map(Chat::getPeep).toList();

        // 모든 핍을 하나로 합치고 중복 제거
        Set<Peep> totalPeepSet = new HashSet<>();
        totalPeepSet.addAll(reactedPeeps);
        totalPeepSet.addAll(cattedPeeps);

        // 동네별 그룹화
        Map<String, List<Peep>> townGroupedMap = totalPeepSet.stream()
                .filter(p -> p.getCode() != null)
                .collect(Collectors.groupingBy(p -> String.valueOf(p.getCode())));

        // 상위 3개 동네 추출
        List<String> topTownLegalCodes = townGroupedMap.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        // 각 동네별 Peep 10개 최신순
        Map<String, List<CommonPeepDto>> peepsByTown = new HashMap<>();
        Map<String, String> topTowns = new HashMap<>();

        for(String townCode : topTownLegalCodes) {
            List<Peep> peepList = townGroupedMap.getOrDefault(townCode, List.of());
            List<CommonPeepDto> dtoList = peepList.stream()
                    .sorted(Comparator.comparing(Peep::getCreatedAt).reversed())
                    .limit(10)
                    .map(p -> CommonPeepDto.builder()
                            .peepId(p.getId())
                            .memberId(p.getMember().getId())
                            .town(p.getTown())
                            .longitude(p.getPeepLocation().getLongitude())
                            .latitude(p.getPeepLocation().getLatitude())
                            .building(p.getPeepLocation().getBuilding())
                            .imageUrl(p.getImageUrl())
                            .content(p.getContent())
                            .isEdited(p.getIsEdited())
                            .profileUrl(p.getMember().getProfileImg())
                            .isActive(timeAgoUtils.isActiveWithin24Hours(p.getCreatedAt()))
                            .uploadAt(timeAgoUtils.getTimeAgo(p.getCreatedAt()))
                            .stickerNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                            .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                            .popularity(p.calculatePopularityScore())
                            .isVideo(p.getIsVideo())
                            .build())
                    .toList();
            peepsByTown.put(townCode, dtoList);
            topTowns.put(townCode, stateRepository.findByCode(townCode).get().getName());
        }

        if (peepsByTown.isEmpty()) {
            return CommonResponse.failed(CustomError.PEEP_NOT_FOUND);
        }

        // response
        ResponsePeepsByTownDto responseDto = ResponsePeepsByTownDto.builder()
                .topTowns(topTowns)
                .peepsByTown(peepsByTown)
                .build();

        // response 반환
        return CommonResponse.ok(responseDto);
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
                    .longitude(p.getPeepLocation().getLongitude())
                    .latitude(p.getPeepLocation().getLatitude())
                    .building(p.getPeepLocation().getBuilding())
                    .imageUrl(p.getImageUrl())
                    .content(p.getContent())
                    .isEdited(p.getIsEdited())
                    .profileUrl(p.getMember().getProfileImg())
                    .isActive(timeAgoUtils.isActiveWithin24Hours(p.getCreatedAt()))
                    .uploadAt(timeAgoUtils.getTimeAgo(p.getCreatedAt()))
                    .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                    .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                    .popularity(p.calculatePopularityScore())
                    .isVideo(p.getIsVideo())
                    .build();
        });

        if (responseDtoPage.isEmpty()) {
            return CommonResponse.failed(CustomError.PEEP_NOT_FOUND);
        }

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
                    .longitude(p.getPeepLocation().getLongitude())
                    .latitude(p.getPeepLocation().getLatitude())
                    .building(p.getPeepLocation().getBuilding())
                    .imageUrl(p.getImageUrl())
                    .content(p.getContent())
                    .isEdited(p.getIsEdited())
                    .profileUrl(p.getMember().getProfileImg())
                    .isActive(timeAgoUtils.isActiveWithin24Hours(p.getCreatedAt()))
                    .uploadAt(timeAgoUtils.getTimeAgo(p.getCreatedAt()))
                    .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                    .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                    .popularity(p.calculatePopularityScore())
                    .isVideo(p.getIsVideo())
                    .build();
        });

        if (responseDtoPage.isEmpty()) {
            return CommonResponse.failed(CustomError.PEEP_NOT_FOUND);
        }

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
                .longitude(p.getPeepLocation().getLongitude())
                .latitude(p.getPeepLocation().getLatitude())
                .building(p.getPeepLocation().getBuilding())
                .imageUrl(p.getImageUrl())
                .content(p.getContent())
                .isEdited(p.getIsEdited())
                .profileUrl(p.getMember().getProfileImg())
                .isActive(timeAgoUtils.isActiveWithin24Hours(p.getCreatedAt()))
                .uploadAt(timeAgoUtils.getTimeAgo(p.getCreatedAt()))
                .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                .popularity(p.calculatePopularityScore())
                .isVideo(p.getIsVideo())
                .build());

        if (responseDtoPage.isEmpty()) {
            return CommonResponse.failed(CustomError.PEEP_NOT_FOUND);
        }

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

    /*
    사용자 활성 핍 리스트 조회
     */
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getActivePeepList(int page, int size) {
        // 1. 현재 로그인 사용자 ID 조회
        String memberId = userInfo.getCurrentMemberUid();

        // 2. 페이징 처리를 위한 PageRequest 생성 (최신순 정렬)
        Pageable pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "activeTime"));

        // 3. `activeTime`이 24시간 이내이면서 `memberId`가 일치하는 `Peep`만 조회
        Page<Peep> peepPage = peepRepository.findAllByActiveTimeAfterAndMember_Id(
                LocalDateTime.now().minusHours(24), memberId, pageRequest
        );

        // 4. `Peep`에서 `CommonPeepDto`로 변환하며 인기순으로 정렬
        List<CommonPeepDto> sortedPeepList = peepPage.getContent().stream()
                .sorted(Comparator.comparingDouble(Peep::calculatePopularityScore).reversed()) // 🔥 인기순 정렬
                .map(p -> CommonPeepDto.builder()
                        .peepId(p.getId())
                        .memberId(p.getMember().getId())
                        .town(p.getCode().getName())
                        .longitude(p.getPeepLocation().getLongitude())
                        .latitude(p.getPeepLocation().getLatitude())
                        .building(p.getPeepLocation().getBuilding())
                        .imageUrl(p.getImageUrl())
                        .content(p.getContent())
                        .isEdited(p.getIsEdited())
                        .profileUrl(p.getMember().getProfileImg())
                        .isActive(true)
                        .uploadAt(timeAgoUtils.getTimeAgo(p.getActiveTime()))
                        .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                        .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                        .popularity(p.calculatePopularityScore())
                        .build())
                .toList(); // ✅ 리스트 변환

        if (sortedPeepList.isEmpty()) {
            return CommonResponse.failed(CustomError.PEEP_NOT_FOUND);
        }

        // 5. 정렬된 데이터를 기반으로 새로운 Page 객체 생성
        Page<CommonPeepDto> sortedPage = new PageImpl<>(sortedPeepList, pageRequest, sortedPeepList.size());

        // 6. PagedResponse 객체 생성
        PagedResponse<CommonPeepDto> pagedResponse = PagedResponse.create(
                sortedPage.getContent(),
                sortedPage.getNumber(),
                sortedPage.getSize(),
                sortedPage.getTotalPages(),
                sortedPage.getTotalElements()
        );

        // 7. Response 반환
        return CommonResponse.ok(pagedResponse);
    }

    /*
    인기 핍 리스트 조회
     */
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getHotPeepList(int page, int size) {
        // 1. 현재 로그인 사용자의 등록 동네 조회
        Member member = userInfo.getCurrentMember();

        Town town = member.getTown();
        if (town == null || town.getState() == null) {
            log.info("error custom 필요");
            return CommonResponse.failed(CustomError.TOWN_NOT_FOUND);
        }

        State memberState = town.getState();

        // 2. 페이징 처리를 위한 PageRequest 생성
        PageRequest pageRequest = PageRequest.of(page, size);

        // 3. `code`가 `memberState`와 일치 & `activeTime`이 24시간 이내인 Peep 조회
        Page<Peep> peepPage = peepRepository.findAllByCodeAndActiveTimeAfter(
                memberState, LocalDateTime.now().minusHours(24), pageRequest
        );

        // 4. `Peep`에서 `CommonPeepDto`로 변환 후, 인기도 순으로 정렬
        List<CommonPeepDto> sortedPeepList = peepPage.getContent().stream()
                .sorted(Comparator.comparingDouble(Peep::calculatePopularityScore).reversed())
                .map(p -> CommonPeepDto.builder()
                        .peepId(p.getId())
                        .memberId(p.getMember().getId())
                        .town(p.getTown())
                        .longitude(p.getPeepLocation().getLongitude())
                        .latitude(p.getPeepLocation().getLatitude())
                        .building(p.getPeepLocation().getBuilding())
                        .imageUrl(p.getImageUrl())
                        .content(p.getContent())
                        .isEdited(p.getIsEdited())
                        .profileUrl(p.getMember().getProfileImg())
                        .isActive(true)
                        .uploadAt(timeAgoUtils.getTimeAgo(p.getCreatedAt()))
                        .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                        .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                        .popularity(p.calculatePopularityScore())
                        .isVideo(p.getIsVideo())
                        .build())
                .toList();

        if (sortedPeepList.isEmpty()) {
            return CommonResponse.failed(CustomError.PEEP_NOT_FOUND);
        }

        // 5. 기존 페이지의 전체 개수를 기준으로 Page 객체 생성 (수정 포인트)
        Page<CommonPeepDto> sortedPage = new PageImpl<>(sortedPeepList, pageRequest, peepPage.getTotalElements());

        // 6. PagedResponse 객체 생성
        PagedResponse<CommonPeepDto> pagedResponse = PagedResponse.create(
                sortedPage.getContent(),
                sortedPage.getNumber(),
                sortedPage.getSize(),
                sortedPage.getTotalPages(),
                sortedPage.getTotalElements()
        );

        // 7. response 반환
        return CommonResponse.ok(pagedResponse);
    }

    /*
    동네 실시간 핍 리스트 조회 (최신순)
     */
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getTownPeepList(int page, int size) {
        // 1. 현재 로그인 사용자의 등록 동네 조회
        Member member = userInfo.getCurrentMember();

        Town town = member.getTown();
        if (town == null || town.getState() == null) {
            log.info("error custom 필요");
//            throw new RuntimeException("사용자의 동네 정보가 없습니다");
            return CommonResponse.failed(CustomError.TOWN_NOT_FOUND);
        }

        State memberState = town.getState();


        // 2. 페이징 처리를 위한 PageRequest 생성 (최신순 정렬 추가)
//        PageRequest pageRequest = PageRequest.of(page, size);
        PageRequest pageRequest = PageRequest.of(
                page, size, Sort.by("createdAt").descending()
        );

        // 3. `code`가 `memberState`와 일치 & `activeTime`이 24시간 이내인 Peep 조회
        Page<Peep> peepPage = peepRepository.findAllByCodeAndActiveTimeAfter(
                memberState, LocalDateTime.now().minusHours(24), pageRequest
        );

        if (peepPage.isEmpty()) {
            return CommonResponse.failed(CustomError.PEEP_NOT_FOUND);
        }

        // 3. `Peep`에서 `CommonPeepDto`로 변환
        List<CommonPeepDto> sortedPeepList = peepPage.getContent().stream()
                .sorted(Comparator.comparing(Peep::getCreatedAt).reversed())
                .map(p -> CommonPeepDto.builder()
                        .peepId(p.getId())
                        .memberId(p.getMember().getId())
                        .town(p.getTown())
                        .longitude(p.getPeepLocation().getLongitude())
                        .latitude(p.getPeepLocation().getLatitude())
                        .building(p.getPeepLocation().getBuilding())
                        .imageUrl(p.getImageUrl())
                        .content(p.getContent())
                        .isEdited(p.getIsEdited())
                        .profileUrl(p.getMember().getProfileImg())
                        .isActive(true)
                        .uploadAt(timeAgoUtils.getTimeAgo(p.getCreatedAt()))
                        .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                        .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                        .popularity(p.calculatePopularityScore())
                        .isVideo(p.getIsVideo())
                        .build())
                .toList();

        // 4. 정렬된 데이터를 기반으로 새로운 Page 객체 생성
        Page<CommonPeepDto> sortedPage = new PageImpl<>(sortedPeepList, pageRequest, peepPage.getTotalElements());

        // 5. PagedResponse 객체 생성
        PagedResponse<CommonPeepDto> pagedResponse = PagedResponse.create(
                sortedPage.getContent(),
                sortedPage.getNumber(),
                sortedPage.getSize(),
                sortedPage.getTotalPages(),
                sortedPage.getTotalElements()
        );

        // 6. response 반환
        return CommonResponse.ok(pagedResponse);
    }

    /*
    지도 내 핍 리스트 조회 (인기도순만 적용)
    */
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getMapPeepList(int dist, int page, int size, double latitude, double longitude, String legalCode) {
        // 1. 현재 로그인 사용자의 등록 동네(State) 조회
//        Member member = userInfo.getCurrentMember();
        Optional<State> optionalState = stateRepository.findByCode(legalCode);
        if (optionalState.isEmpty()) {
            log.info("error custom 필요");
            return CommonResponse.failed(CustomError.TOWN_NOT_FOUND);
        }

        String memberCode = optionalState.get().getCode();

        // 2. 거리 + 시간 + 지역 조건에 맞는 모든 핍 조회
        List<Peep> peepList = peepRepository.findAllNearbyPeeps(
                longitude, latitude,
                dist,
//                LocalDateTime.now().minusHours(24),
                LocalDateTime.now().minusHours(24000),
                memberCode
        );

        // 3. 인기도 점수 기준 정렬 (내림차순)
        List<Peep> sorted = peepList.stream()
                .sorted(Comparator.comparingDouble(Peep::calculatePopularityScore).reversed())
                .toList();

        // 4. 자바에서 슬라이싱 방식으로 페이징 처리
        int start = page * size;
        int end = Math.min(start + size, sorted.size());
        if (start >= end) {
            return CommonResponse.failed(CustomError.PEEP_NOT_FOUND);
        }

        List<CommonPeepDto> pagedDtos = sorted.subList(start, end).stream()
                .map(p -> CommonPeepDto.builder()
                        .peepId(p.getId())
                        .memberId(p.getMember().getId())
                        .town(p.getTown())
                        .longitude(p.getPeepLocation().getLongitude())
                        .latitude(p.getPeepLocation().getLatitude())
                        .building(p.getPeepLocation().getBuilding())
                        .imageUrl(p.getImageUrl())
                        .content(p.getContent())
                        .isEdited(p.getIsEdited())
                        .profileUrl(p.getMember().getProfileImg())
                        .isActive(timeAgoUtils.isActiveWithin24Hours(p.getCreatedAt())) // ✅ 활성 상태 계산
                        .uploadAt(timeAgoUtils.getTimeAgo(p.getCreatedAt()))
                        .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                        .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                        .popularity(p.calculatePopularityScore())
                        .isVideo(p.getIsVideo())
                        .build())
                .toList();

        // 5. PageImpl 생성
        Page<CommonPeepDto> resultPage = new PageImpl<>(pagedDtos, PageRequest.of(page, size), sorted.size());

        // 6. PagedResponse 생성
        PagedResponse<CommonPeepDto> pagedResponse = PagedResponse.create(
                resultPage.getContent(),
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalPages(),
                resultPage.getTotalElements()
        );

        // 7. 응답 반환
        return CommonResponse.ok(pagedResponse);
    }

    /*
    사용자의 모든 유관 핍 리스트 조회
     */
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getTotalPeepList(int page, int size) {
        // 현재 로그인 사용자 ID 조회
        String memberId = userInfo.getCurrentMemberUid();

        // 업로드한 핍
        List<Peep> uploadedPeeps = peepRepository.findAllByMember_Id(memberId);


        // 반응한 핍
        List<Peep> reactedPeeps = peepReStickerRepository.findAllByMember_Id(memberId)
                .stream().map(PeepReSticker::getPeep).toList();

        // 댓글단 핍
        List<Peep> cattedPeeps = chatRepository.findDistinctPeepsByMemberId(memberId)
                .stream().map(Chat::getPeep).toList();

        // 모든 핍을 하나로 합치고 중복 제거
        Set<Peep> totalPeepSet = new HashSet<>();
        totalPeepSet.addAll(uploadedPeeps);
        totalPeepSet.addAll(reactedPeeps);
        totalPeepSet.addAll(cattedPeeps);

        // 최신순 정렬
        List<Peep> sortedPeepList = totalPeepSet.stream()
                .sorted(Comparator.comparing(Peep::getCreatedAt).reversed())
                .toList();

        // 페이징 처리
        int start = page * size;
        int end = Math.min(start + size, sortedPeepList.size());
        List<Peep> pagedPeeps = start >= sortedPeepList.size() ? Collections.emptyList() : sortedPeepList.subList(start, end);

        // dto 변환
        List<CommonPeepDto> dtoList = pagedPeeps.stream().map(p -> CommonPeepDto.builder()
                .peepId(p.getId())
                .memberId(p.getMember().getId())
                .town(p.getTown())
                .longitude(p.getPeepLocation().getLongitude())
                .latitude(p.getPeepLocation().getLatitude())
                .building(p.getPeepLocation().getBuilding())
                .imageUrl(p.getImageUrl())
                .content(p.getContent())
                .isEdited(p.getIsEdited())
                .profileUrl(p.getMember().getProfileImg())
                .isActive(timeAgoUtils.isActiveWithin24Hours(p.getCreatedAt()))
                .uploadAt(timeAgoUtils.getTimeAgo(p.getCreatedAt()))
                .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                .popularity(p.calculatePopularityScore())
                .isVideo(p.getIsVideo())
                .isVideo(p.getIsVideo())
                .build()).toList();

        if (pagedPeeps.isEmpty()) {
            return CommonResponse.failed(CustomError.PEEP_NOT_FOUND);
        }

        // PagedResponse 생성
        PagedResponse<CommonPeepDto> pagedResponse = PagedResponse.create(
                dtoList,
                page,
                size,
                (int) Math.ceil((double) totalPeepSet.size() / size),
                totalPeepSet.size()
        );

        // response 반환
        return CommonResponse.ok(pagedResponse);
    }

    /*
    사용자가 댓글단/반응한 핍 리스트 페이지네이션
    - 개별 동네에 대한 조회
     */
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getPeepListByTown(String townCode, int page, int size) {
        String memberId = userInfo.getCurrentMemberUid();

        // 반응한 핍
        List<Peep> reactedPeeps = peepReStickerRepository.findAllByMember_Id(memberId)
                .stream().map(PeepReSticker::getPeep).toList();

        // 댓글단 핍
        List<Peep> cattedPeeps = chatRepository.findDistinctPeepsByMemberId(memberId)
                .stream().map(Chat::getPeep).toList();

        // 합치고 중복 제거
        Set<Peep> totalPeepSet = new HashSet<>();
        totalPeepSet.addAll(reactedPeeps);
        totalPeepSet.addAll(cattedPeeps);

        // 동네 코드로 필터링
        List<Peep> filteredPeepList = totalPeepSet.stream()
                .filter(p -> p.getCode() != null && p.getCode().equals(townCode))
                .sorted(Comparator.comparing(Peep::getCreatedAt).reversed())
                .toList();

        // 페이징 처리
        int start = page * size;
        int end = Math.min(start + size, filteredPeepList.size());
        List<Peep> pagedPeeps = start >= filteredPeepList.size() ? List.of() : filteredPeepList.subList(start, end);

        // DTO 변환
        List<CommonPeepDto> dtoList = pagedPeeps.stream().map(p -> CommonPeepDto.builder()
                .peepId(p.getId())
                .memberId(p.getMember().getId())
                .town(p.getTown())
                .longitude(p.getPeepLocation().getLongitude())
                .latitude(p.getPeepLocation().getLatitude())
                .building(p.getPeepLocation().getBuilding())
                .imageUrl(p.getImageUrl())
                .content(p.getContent())
                .isEdited(p.getIsEdited())
                .profileUrl(p.getMember().getProfileImg())
                .isActive(timeAgoUtils.isActiveWithin24Hours(p.getCreatedAt()))
                .uploadAt(timeAgoUtils.getTimeAgo(p.getCreatedAt()))
                .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                .popularity(p.calculatePopularityScore())
                .isVideo(p.getIsVideo())
                .build()).toList();

        if (pagedPeeps.isEmpty()) {
            return CommonResponse.failed(CustomError.PEEP_NOT_FOUND);
        }

        // 응답 포장
        PagedResponse<CommonPeepDto> response = PagedResponse.create(
                dtoList,
                page,
                size,
                (int) Math.ceil((double) filteredPeepList.size() / size),
                filteredPeepList.size()
        );

        return CommonResponse.ok(response);
    }

    /*
    업로드한 핍 조회 (동네별)
     */
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getUploadedPeepListByTown(String townCode, int page, int size) {
        // 현재 로그인 사용자 ID 조회
        String memberId = userInfo.getCurrentMemberUid();

        // 업로드한 핍
        List<Peep> uploadedPeeps = peepRepository.findAllByMember_Id(memberId);

        // 동네 코드로 필터링
        List<Peep> filteredPeepList = uploadedPeeps.stream()
                .filter(p -> p.getCode() != null && p.getCode().equals(townCode))
                .sorted(Comparator.comparing(Peep::getCreatedAt).reversed())
                .toList();

        // 페이징 처리
        int start = page * size;
        int end = Math.min(start + size, filteredPeepList.size());
        List<Peep> pagedPeeps = start >= filteredPeepList.size() ? List.of() : filteredPeepList.subList(start, end);

        // DTO 변환
        List<CommonPeepDto> dtoList = pagedPeeps.stream().map(p -> CommonPeepDto.builder()
                .peepId(p.getId())
                .memberId(p.getMember().getId())
                .town(p.getTown())
                .longitude(p.getPeepLocation().getLongitude())
                .latitude(p.getPeepLocation().getLatitude())
                .building(p.getPeepLocation().getBuilding())
                .imageUrl(p.getImageUrl())
                .content(p.getContent())
                .isEdited(p.getIsEdited())
                .profileUrl(p.getMember().getProfileImg())
                .isActive(timeAgoUtils.isActiveWithin24Hours(p.getCreatedAt()))
                .uploadAt(timeAgoUtils.getTimeAgo(p.getCreatedAt()))
                .stickerNum(Optional.ofNullable(p.getPeepReStickerList()).map(List::size).orElse(0))
                .chatNum(Optional.ofNullable(p.getChatList()).map(List::size).orElse(0))
                .popularity(p.calculatePopularityScore())
                .isVideo(p.getIsVideo())
                .build()).toList();

        if (pagedPeeps.isEmpty()) {
            return CommonResponse.failed(CustomError.PEEP_NOT_FOUND);
        }

        // 응답 포장
        PagedResponse<CommonPeepDto> response = PagedResponse.create(
                dtoList,
                page,
                size,
                (int) Math.ceil((double) filteredPeepList.size() / size),
                filteredPeepList.size()
        );

        return CommonResponse.ok(response);
    }
}
