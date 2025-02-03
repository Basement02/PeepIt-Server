package com.b02.peep_it.controller;

import com.b02.peep_it.common.response.ApiResponse;
import com.b02.peep_it.common.response.PagedResponse;
import com.b02.peep_it.dto.CommonPeepDto;
import com.b02.peep_it.dto.peep.RequestPeepUploadDto;
import com.b02.peep_it.service.PeepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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
    @Operation(summary = "신규 핍 등록 (텍스트 + 이미지/영상)", description = "이미지와 함께 새로운 핍을 업로드합니다.")
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
    @Operation(summary = "개별 핍 조회", description = "개별 핍을 조회합니다.")
    @GetMapping("/{peepId}")
    public ApiResponse<CommonPeepDto> getPeep(@PathVariable("peepId") Long peepId) {
        return peepService.getPeepById(peepId);
    }

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

    // 사용자가 업로드한 핍 리스트 조회
    @Operation(summary = "내가 업로드한 핍 리스트 조회", description = "내가 업로드한 핍을 리스트로 조회합니다.")
    @GetMapping("/my/upload")
    public ApiResponse<PagedResponse<CommonPeepDto>> getMyPeepList(@RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
        return peepService.getMyUploadPeepList(page, size);
    }
    // 내가 반응한 핍 리스트 조회
    // 내가 댓글 단 핍 리스트 조회
    // 특정 사용자가 업로드한 핍 리스트 조회
    // 내 실시간 활성 핍 리스트 조회
    // 인기 핍 리스트 조회
    // 지도 내 핍 리스트 조회
}
