package com.b02.peep_it.controller;

import com.b02.peep_it.common.ApiResponse;
import com.b02.peep_it.dto.CommonPeepDto;
import com.b02.peep_it.dto.peep.RequestPeepUploadDto;
import com.b02.peep_it.service.PeepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Peep API", description = "핍 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/peep")
public class PeepController {
    private final PeepService peepService;

    /*
    신규 핍 등록 (텍스트 + 이미지/영상)
     */
    @Operation(summary = "핍 업로드", description = "이미지와 함께 새로운 핍을 업로드합니다.")
    @PostMapping("/post")
    public ApiResponse<CommonPeepDto> uploadPeep(
            @RequestPart("peepData") RequestPeepUploadDto requestDto,
            @RequestPart("media") MultipartFile media) {
        return peepService.createPeep(requestDto, media);
    }

    /*
    스티커 목록 조회
     */

    /*
    개별 핍 조회
     */

    /*
    핍 리스트 조회
    - 내가 업로드한 핍 리스트 조회
    - 내가 반응한 핍 리스트 조회
    - 내가 댓글 단 핍 리스트 조회
    - 특정 사용자가 업로드한 핍 리스트 조회
    - 내 실시간 활성 핍 리스트 조회
    - 인기 핍 리스트 조회
    - 지도 내 핍 리스트 조회
     */

    // 내가 업로드한 핍 리스트 조회
    // 내가 반응한 핍 리스트 조회
    // 내가 댓글 단 핍 리스트 조회
    // 특정 사용자가 업로드한 핍 리스트 조회
    // 내 실시간 활성 핍 리스트 조회
    // 인기 핍 리스트 조회
    // 지도 내 핍 리스트 조회
}
