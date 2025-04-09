package com.b02.peep_it.controller;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.response.PagedResponse;
import com.b02.peep_it.dto.peep.CommonPeepDto;
import com.b02.peep_it.dto.peep.RequestPeepUploadDto;
import com.b02.peep_it.dto.peep.ResponsePeepsByTownDto;
import com.b02.peep_it.service.PeepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
    - 나의 반응/업로드 핍 리스트 조회
    - 나의 반응/업로드 핍 리스트 조회 (동네별)
    - 내가 업로드한 핍 리스트 조회
    - 내가 반응한 핍 리스트 조회
    - 내가 댓글 단 핍 리스트 조회
    - 특정 사용자가 업로드한 핍 리스트 조회
    - 내 실시간 활성 핍 리스트 조회
    - 인기 핍 리스트 조회
    - 지도 내 핍 리스트 조회
     */

    // 나의 모든(업로드/반응/댓글) 핍 리스트 조회 (전체)
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "사용자 업로드/반응/댓글 핍 리스트 조회", description = "사용자가 업로드한/반응한/댓글 단 핍을 모두 리스트로 조회합니다.")
    @GetMapping("/my/all")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getMyTotalPeepList(@RequestParam(defaultValue = "0") int page,
                                                                                           @RequestParam(defaultValue = "10") int size) {
        return peepService.getTotalPeepList(page, size);
    }

    // 나의 반응/댓글 핍 리스트 조회 (전체)
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "사용자 반응/댓글 핍 리스트 조회",
            description = """
                    사용자가 반응한/댓글 단 핍을 리스트로 조회합니다.
                    - 모든 요청에서 액션 보유 상위 3개 동네의 동네명을 반환합니다.
                    - 가장 많은 액션을 보유한 상위 3개 동네의 리스트를 10개씩만 반환합니다.
                    """,
            responses = {
            @ApiResponse(responseCode = "200",
                    description = "성공적으로 핍 조회",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "data": {
                                                "topTowns": {
                                                  "11680": "서울특별시 강남구",
                                                  "41135": "경기도 성남시 분당구",
                                                  "26440": "부산광역시 해운대구"
                                                },
                                                "peepsByTown": {
                                                  "11680": [
                                                    {
                                                      "peepId": 101,
                                                      "memberId": "gangjjang5",
                                                      "town": {
                                                        "code": "11680",
                                                        "stateName": "서울특별시 강남구"
                                                      },
                                                      "imageUrl": "https://cdn.peepit.com/images/peep101.jpg",
                                                      "content": "오늘 강남 하늘 진짜 예쁘다 🌤",
                                                      "isEdited": false,
                                                      "profileUrl": "https://cdn.peepit.com/profiles/user5.jpg",
                                                      "isActive": true,
                                                      "uploadAt": "2시간 전",
                                                      "stickerNum": 5,
                                                      "chatNum": 3
                                                    }
                                                  ],
                                                  "41135": [
                                                    {
                                                      "peepId": 88,
                                                      "memberId": "gangjjang5",
                                                      "town": {
                                                        "code": "41135",
                                                        "stateName": "경기도 성남시 분당구"
                                                      },
                                                      "imageUrl": "https://cdn.peepit.com/images/peep88.jpg",
                                                      "content": "정자동 카페거리에서 발견한 분위기 좋은 곳 ☕️",
                                                      "isEdited": true,
                                                      "profileUrl": "https://cdn.peepit.com/profiles/user5.jpg",
                                                      "isActive": true,
                                                      "uploadAt": "5시간 전",
                                                      "stickerNum": 4,
                                                      "chatNum": 2
                                                    }
                                                  ],
                                                  "26440": [
                                                    {
                                                      "peepId": 77,
                                                      "memberId": "gangjjang5",
                                                      "town": {
                                                        "code": "26440",
                                                        "stateName": "부산광역시 해운대구"
                                                      },
                                                      "imageUrl": "https://cdn.peepit.com/images/peep77.jpg",
                                                      "content": "해운대 바다 보고 힐링 🌊",
                                                      "isEdited": false,
                                                      "profileUrl": "https://cdn.peepit.com/profiles/user5.jpg",
                                                      "isActive": false,
                                                      "uploadAt": "1일 전",
                                                      "stickerNum": 1,
                                                      "chatNum": 0
                                                    }
                                                  ]
                                                }
                                              },
                                              "error": null
                                            }
                                            """)))
            }
    )
    @GetMapping("/my/actions")
    public ResponseEntity<CommonResponse<ResponsePeepsByTownDto>> getMyActionList() {
        return peepService.getActionPeepList();
    }

    // 나의 댓글단/반응한 핍 리스트 조회 (동네별)
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "사용자 반응/댓글 핍 리스트 조회 (동네 지정)",
            description = """
                    요청한 동네에 대해 사용자가 반응한/댓글 단 핍을 리스트로 조회합니다.
                    - 페이지네이션 default: page - 0, size - 10
                    """,
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "성공적으로 핍 조회",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": {
                                                        "content": [
                                                          {
                                                            "peepId": 101,
                                                            "memberId": "gangjjang5",
                                                            "town": {
                                                              "code": "11680",
                                                              "stateName": "서울특별시 강남구"
                                                            },
                                                            "imageUrl": "https://cdn.peepit.com/images/peep101.jpg",
                                                            "content": "오늘 강남 하늘 진짜 예쁘다 🌤",
                                                            "isEdited": false,
                                                            "profileUrl": "https://cdn.peepit.com/profiles/user5.jpg",
                                                            "isActive": true,
                                                            "uploadAt": "2시간 전",
                                                            "stickerNum": 5,
                                                            "chatNum": 3
                                                          },
                                                          {
                                                            "peepId": 99,
                                                            "memberId": "gangjjang5",
                                                            "town": {
                                                              "code": "11680",
                                                              "stateName": "서울특별시 강남구"
                                                            },
                                                            "imageUrl": "https://cdn.peepit.com/images/peep99.jpg",
                                                            "content": "강남역에서 만난 강아지 🐶",
                                                            "isEdited": true,
                                                            "profileUrl": "https://cdn.peepit.com/profiles/user5.jpg",
                                                            "isActive": false,
                                                            "uploadAt": "1일 전",
                                                            "stickerNum": 2,
                                                            "chatNum": 1
                                                          }
                                                        ],
                                                        "page": 0,
                                                        "size": 10,
                                                        "totalPages": 3,
                                                        "totalElements": 25
                                                      },
                                                      "error": null
                                                    }
                                                    """)))
            }
    )
    @GetMapping("/my/actions/{town}")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getPeepListByTown(@PathVariable("town") String town,
                                                                                          @RequestParam(defaultValue = "0") int page,
                                                                                          @RequestParam(defaultValue = "10") int size
    ) {
        return peepService.getPeepListByTown(town, page, size);
    }


    // 사용자가 업로드한 핍 리스트 조회 (전체)
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "사용자가 업로드한 핍 리스트 조회", description = "사용자가 업로드한 핍을 리스트로 조회합니다.")
    @GetMapping("/my/upload")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getMyDefaultPeepList(@RequestParam(defaultValue = "0") int page,
                                                                                      @RequestParam(defaultValue = "10") int size) {
        return peepService.getUploadedPeepList(page, size);
    }

    // 사용자가 반응한 핍 리스트 조회 (전체)
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "사용자가 반응한 핍 리스트 조회", description = "사용자가 반응한 핍 리스트를 조회합니다.")
    @GetMapping("/my/react")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getMyReactedPeepList(@RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size) {
        return peepService.getReactedPeepList(page, size);
    }

    // 사용자가 댓글 단 핍 리스트 조회 (전체)
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "사용자가 댓글 단 핍 리스트 조회", description = "사용자가 댓글 단 핍 리스트를 조회합니다.")
    @GetMapping("/my/chat")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getMyChatPeepList(@RequestParam(defaultValue = "0") int page,
                                                                                          @RequestParam(defaultValue = "10") int size) {
        return peepService.getChatPeepList(page, size);
    }

    // 사용자 실시간 활성 핍 리스트 조회 (전체)
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "사용자가 업로드한 실시간 핍 리스트 조회", description = "사용자가 업로드한 활성화 상태의 핍 리스트를 조회합니다.")
    @GetMapping("/my/active")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getMyActivePeepList(@RequestParam(defaultValue = "0") int page,
                                                                                            @RequestParam(defaultValue = "10") int size) {
        return peepService.getActivePeepList(page, size);
    }

    // 특정 사용자가 업로드한 핍 리스트 조회 (전체)
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "특정 사용자가 업로드한 핍 리스트 조회", description = "특정 사용자가 업로드한 핍 리스트를 조회합니다.")
    @GetMapping("/get")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getMemberPeepList(@RequestParam("memberId") String memberId,
                                                                                          @RequestParam(defaultValue = "0") int page,
                                                                                          @RequestParam(defaultValue = "10") int size) {
        return peepService.getMemberPeepList(memberId, page, size);
    }

    // 인기 핍 리스트 조회 (전체)
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "인기 핍 리스트 조회", description = "실시간 인기 핍으로 선정된 핍 리스트를 조회합니다.")
    @GetMapping("/get/hot")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getHotPeepList(@RequestParam(defaultValue = "0") int page,
                                                                                       @RequestParam(defaultValue = "10") int size) {
        return peepService.getHotPeepList(page, size);
    }

    // 실시간 핍 리스트 조회 (동네별)
    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "동네 핍 리스트 조회 (최신순)", description = "동네에 등록된 실시간 핍 리스트를 최신순으로 조회합니다.")
    @GetMapping("/get/town")
    public ResponseEntity<CommonResponse<PagedResponse<CommonPeepDto>>> getTownPeepList(@RequestParam(defaultValue = "0") int page,
                                                                                       @RequestParam(defaultValue = "10") int size) {
        return peepService.getTownPeepList(page, size);
    }

    // 지도 내 핍 리스트 조회 (전체)
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
