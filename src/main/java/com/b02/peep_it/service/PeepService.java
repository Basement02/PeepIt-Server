package com.b02.peep_it.service;

import com.b02.peep_it.common.ApiResponse;
import com.b02.peep_it.common.exception.CustomError;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public ApiResponse<CommonPeepDto> createPeep(
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
            return ApiResponse.created(responseDto);

        } catch (IOException e) {
            return ApiResponse.failed(CustomError.NEED_TO_CUSTOM);
//            throw new RuntimeException(e);
        }
    }

}
