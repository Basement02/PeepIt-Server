package com.b02.peep_it.controller;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.response.PagedResponse;
import com.b02.peep_it.dto.peep.CommonPeepDto;
import com.b02.peep_it.dto.peep.RequestPeepUploadDto;
import com.b02.peep_it.service.PeepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Peep API", description = "핍 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/peep")
public class PeepController {
    private final PeepService peepService;

    /*
    신규 핍 등록 (텍스트 + 이미지/영상)
     */
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "신규 핍 등록 (텍스트 + 이미지/영상)", description = "이미지와 함께 새로운 핍을 업로드합니다.")
    @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<CommonPeepDto>> uploadPeep(
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
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "개별 핍 조회", description = "개별 핍을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청 성공 - 핍 조회 완료"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 핍 ID",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/get/{peepId}")
    public ResponseEntity<CommonResponse<CommonPeepDto>> getPeep(@PathVariable("peepId") Long peepId) {
        return peepService.getPeepById(peepId);
    }

    /*
    핍 리스트 조회
    - 나의 모든 핍 리스트 조회
    - 내가 업로드한 핍 리스트 조회
    - 내가 반응한 핍 리스트 조회
    - 내가 댓글 단 핍 리스트 조회
    - 특정 사용자가 업로드한 핍 리스트 조회
    - 내 실시간 활성 핍 리스트 조회
    - 인기 핍 리스트 조회
    - 지도 내 핍 리스트 조회
     */

    // 나의 모든 핍 리스트 조회
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "사용자 업로드/반응/댓글 핍 리스트 조회", description = "사용자가 업로드한/반응한/댓글 단 핍을 모두 리스트로 조회합니다.")
    @GetMapping("/my/all")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getMyTotalPeepList(@RequestParam(defaultValue = "0") int page,
                                                                                           @RequestParam(defaultValue = "10") int size) {
        return peepService.getTotalPeepList(page, size);
    }

    // 사용자가 업로드한 핍 리스트 조회
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "사용자가 업로드한 핍 리스트 조회", description = "사용자가 업로드한 핍을 리스트로 조회합니다.")
    @GetMapping("/my/upload")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getMyDefaultPeepList(@RequestParam(defaultValue = "0") int page,
                                                                                      @RequestParam(defaultValue = "10") int size) {
        return peepService.getUploadedPeepList(page, size);
    }

    // 사용자가 반응한 핍 리스트 조회
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "사용자가 반응한 핍 리스트 조회", description = "사용자가 반응한 핍 리스트를 조회합니다.")
    @GetMapping("/my/react")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getMyReactedPeepList(@RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size) {
        return peepService.getReactedPeepList(page, size);
    }

    // 사용자가 댓글 단 핍 리스트 조회
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "사용자가 댓글 단 핍 리스트 조회", description = "사용자가 댓글 단 핍 리스트를 조회합니다.")
    @GetMapping("/my/chat")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getMyChatPeepList(@RequestParam(defaultValue = "0") int page,
                                                                                          @RequestParam(defaultValue = "10") int size) {
        return peepService.getChatPeepList(page, size);
    }

    // 사용자 실시간 활성 핍 리스트 조회
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "사용자가 업로드한 실시간 핍 리스트 조회", description = "사용자가 업로드한 활성화 상태의 핍 리스트를 조회합니다.")
    @GetMapping("/my/active")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getMyActivePeepList(@RequestParam(defaultValue = "0") int page,
                                                                                            @RequestParam(defaultValue = "10") int size) {
        return peepService.getActivePeepList(page, size);
    }

    // 특정 사용자가 업로드한 핍 리스트 조회
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "특정 사용자가 업로드한 핍 리스트 조회", description = "특정 사용자가 업로드한 핍 리스트를 조회합니다.")
    @GetMapping("/get")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getMemberPeepList(@RequestParam("memberId") String memberId,
                                                                                          @RequestParam(defaultValue = "0") int page,
                                                                                          @RequestParam(defaultValue = "10") int size) {
        return peepService.getMemberPeepList(memberId, page, size);
    }

    // 인기 핍 리스트 조회
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "인기 핍 리스트 조회", description = "실시간 인기 핍으로 선정된 핍 리스트를 조회합니다.")
    @GetMapping("/get/hot")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getHotPeepList(@RequestParam(defaultValue = "0") int page,
                                                                                       @RequestParam(defaultValue = "10") int size) {
        return peepService.getHotPeepList(page, size);
    }

    // 동네 실시간 핍 리스트 조회 (최신순)
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "동네 핍 리스트 조회 (최신순)", description = "동네에 등록된 실시간 핍 리스트를 최신순으로 조회합니다.")
    @GetMapping("/get/town")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getTownPeepList(@RequestParam(defaultValue = "0") int page,
                                                                                       @RequestParam(defaultValue = "10") int size) {
        return peepService.getTownPeepList(page, size);
    }

    // 지도 내 핍 리스트 조회
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "지도 내 핍 리스트 조회", description = "지도 반경 내 노출될 핍 리스트를 조회합니다.")
    @GetMapping("/get/map")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getMapPeepList(@RequestParam(defaultValue = "5") int dist,
                                                                                       @RequestParam(defaultValue = "0") int page,
                                                                                       @RequestParam(defaultValue = "10") int size,
                                                                                       @RequestParam double latitude,
                                                                                       @RequestParam double longitude) {
        return peepService.getMapPeepList(dist, page, size, latitude, longitude);
    }
}
