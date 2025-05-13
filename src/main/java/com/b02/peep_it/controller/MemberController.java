package com.b02.peep_it.controller;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.dto.RequestPatchMemberDto;
import com.b02.peep_it.dto.RequestPatchProfileImgDto;
import com.b02.peep_it.dto.RequestPatchTownDto;
import com.b02.peep_it.dto.member.RequestCommonMemberDto;
import com.b02.peep_it.dto.ResponseLoginDto;
import com.b02.peep_it.dto.member.ResponseCommonMemberDto;
import com.b02.peep_it.service.AuthService;
import com.b02.peep_it.service.MemberService;
import com.b02.peep_it.service.TownService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/member")
public class MemberController {
    private final AuthService authService;
    private final TownService townService;
    private final MemberService memberService;

    /*
    계정 생성
     */
    @SecurityRequirement(name = "RegisterToken")
    @Operation(
            summary = "계정 생성",
            description = """
            - 추가 정보 포함 계정 생성
            - 입력 가능한 추가 정보:
              - id
              - nickname
              - birth
              - gender: 성별
                - female: 여성
                - male: 남성
                - other: 기타
              - isAgree: 마케팅 약관 동의 여부
                - True: 동의
                - False: 거부
        """,
            security = {
                    @SecurityRequirement(name = "AuthToken")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "계정 생성 요청 DTO",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "계정 생성 요청 예시",
                                            value = """
                                            {
                                              "id": "peepit_user1",
                                              "nickname": "PeepItUser",
                                              "birth": "2025-01-01",
                                              "gender": "other",
                                              "isAgree": true
                                            }
                                    """)
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "성공적으로 계정이 생성됨",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseLoginDto.class),
                                    examples = @ExampleObject(
                                            value = """
            {
                "success": true,
                "data": "Schema",
                "error": null
            }
            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "유효하지 않은 소셜 계정",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "data": {},
                        "error": {
                            "code": "40101",
                            "message": "유효하지 않은 social id token 입니다"
                        }
                    }
                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "data": {},
                        "error": {
                            "code": "50000",
                            "message": "서버 내부 오류가 발생했습니다"
                        }
                    }
                    """)
                            )
                    )
            }
    )
    @PostMapping("/sign-up")
    public ResponseEntity<CommonResponse<ResponseLoginDto>> signUp(@RequestBody RequestCommonMemberDto requestDto) throws IOException, InterruptedException {
        log.info("요청받은 DTO: {}", requestDto);
        return authService.createAccount(requestDto);
    }

    /*
    동네 갱신
     */
    @SecurityRequirement(name = "AccessToken")
    @Operation(
            summary = "동네 등록/수정하기 (등록/수정 동일 api)",
            description = """
        - 등록 동네는 오직 1개만 가능
        - 입력 가능한 정보:
            - legalDistrictCode: 법정동 코드 (10자리)
        """,
            security = {
                    @SecurityRequirement(name = "AuthToken")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "동네 등록/수정 요청 DTO",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RequestPatchTownDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "동네 등록/수정 요청 예시",
                                            value = """
                                        {
                                            "legalDistrictCode": "1234512345"
                                        }
                                """)
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "성공적으로 동네가 등록됨",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                {
                                                    "success": true,
                                                    "data": null,
                                                    "error": null
                                                }
                                        """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "40102",
                            description = "유효하지 않은 계정",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                {
                    "success": false,
                    "data": {},
                    "error": {
                        "code": "40101",
                        "message": "유효하지 않은 계정입니다"
                    }
                }
                """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "40402",
                            description = "사용자의 동네 정보가 존재하지 않습니다",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                {
                    "success": false,
                    "data": {},
                    "error": {
                        "code": "40402",
                        "message": "사용자의 동네 정보가 존재하지 않습니다"
                    }
                }
                """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "40403",
                            description = "존재하지 않는 법정동코드입니다",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                {
                    "success": false,
                    "data": {},
                    "error": {
                        "code": "40403",
                        "message": "존재하지 않는 법정동코드입니다"
                    }
                }
                """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                {
                    "success": false,
                    "data": {},
                    "error": {
                        "code": "50000",
                        "message": "서버 내부 오류가 발생했습니다"
                    }
                }
                """)
                            )
                    )
            }
    )
    @PatchMapping("/town")
    public ResponseEntity<CommonResponse<ResponseCommonMemberDto>> patchTown(@RequestBody RequestPatchTownDto requestDto) {
        log.info("요청받은 DTO: {}", requestDto);
        return townService.updateTownInfo(requestDto);
    }

    @SecurityRequirement(name = "AccessToken")
    @Operation(
            summary = "다른 사용자 프로필 조회하기 (아이디/닉네임/동네/프로필이미지/차단여부)",
            description = """
                    - 입력 가능한 정보:
                        - 아이디
                    - isBlocked:
                        - true: 차단한 사용자
                        - false: 차단하지 않은 사용자
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 사용자 정보 조회",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                                    {
                                                                                      "success": true,
                                                                                      "data": {
                                                                                        "id": "gangjjang5",
                                                                                        "role": "UNCERTIFIED",
                                                                                        "gender": "other",
                                                                                        "name": "gangjjang5",
                                                                                        "town": "서울특별시",
                                                                                        "profile": "추후수정필요 프로필 이미지 고정값",
                                                                                        "isAgree": true,
                                                                                        "isBlocked": false
                                                                                      },
                                                                                      "error": null
                                                                                    }
                                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "50000",
                            description = "서버 내부 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                                            {
                                                "success": false,
                                                "data": {},
                                                "error": {
                                                    "code": "50000",
                                                    "message": "서버 내부 오류가 발생했습니다"
                                                }
                                            }
                                            """)
                            )
                    )
            }

    )
    @GetMapping("/detail/{memberId}")
    public ResponseEntity<CommonResponse<ResponseCommonMemberDto>> getMemberDetail(@PathVariable("memberId") String memberId) {
        return memberService.getMemberDetail(memberId);
    }

    /*
    로그인한 사용자 상세 정보 조회
     */
    @SecurityRequirement(name = "AccessToken")
    @Operation(
            summary = "로그인한 사용자 프로필 조회하기 (아이디/닉네임/동네/프로필이미지)",
            description = """
            - 입력 가능한 정보:
                - 없음
            - 역할 상세
                 - CERT("CERTIFIED", "인증 회원"),
                 - UNCERT("UNCERTIFIED", "미인증 회원"),
                 - MAN("MANAGER", "관리자"),
                 - DEV("DEVELOPER", "개발자")
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 사용자 정보 조회",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                                      "success": true,
                                                                      "data": {
                                                                        "id": "gangjjang5",
                                                                        "role": "UNCERTIFIED",
                                                                        "gender": "other",
                                                                        "name": "gangjjang5",
                                                                        "town": "서울특별시",
                                                                        "profile": "추후수정필요 프로필 이미지 고정값",
                                                                        "isAgree": true,
                                                                        "isBlocked": false
                                                                      },
                                                                      "error": null
                                                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "40102",
                            description = "유효하지 않은 계정",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "data": {},
                        "error": {
                            "code": "40102",
                            "message": "유효하지 않은 계정입니다"
                        }
                    }
                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "50000",
                            description = "서버 내부 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "data": {},
                        "error": {
                            "code": "50000",
                            "message": "서버 내부 오류가 발생했습니다"
                        }
                    }
                    """)
                            )
                    )
            }
    )
    @GetMapping("/detail")
    public ResponseEntity<CommonResponse<ResponseCommonMemberDto>> getOwnDetail() {
        return memberService.getMyDetail();
    }

    /*
    사용자 정보 수정
    - 닉네임
    - 성별
    - 생일
    - 마케팅 약관 동의
     */
    @SecurityRequirement(name = "AccessToken")
    @Operation(
            summary = "로그인한 사용자 정보 수정",
            description = """
            - 입력 가능한 정보:
                - 닉네임 nickname
                - 성별 gender
                - 생일 birth
                - 마케팅 약관 동의 isAgree
            - 성별 상세
                 - female
                 - male
                 - other
            """,
//            security = {
//                    @SecurityRequirement(name = "AuthToken")
//            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 사용자 정보 수정",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                                      "success": true,
                                                                      "data": {
                                                                        "id": "gangjjang5",
                                                                        "role": "UNCERTIFIED",
                                                                        "gender": "other",
                                                                        "name": "gangjjang5",
                                                                        "town": "서울특별시",
                                                                        "profile": "추후수정필요 프로필 이미지 고정값",
                                                                        "isAgree": true
                                                                      },
                                                                      "error": null
                                                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "40102",
                            description = "유효하지 않은 계정",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "data": {},
                        "error": {
                            "code": "40102",
                            "message": "유효하지 않은 계정입니다"
                        }
                    }
                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "50000",
                            description = "서버 내부 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "data": {},
                        "error": {
                            "code": "50000",
                            "message": "서버 내부 오류가 발생했습니다"
                        }
                    }
                    """)
                            )
                    )
            }
    )
    @PatchMapping("/detail")
    public ResponseEntity<CommonResponse<ResponseCommonMemberDto>> patchOwnDetail(@RequestBody RequestPatchMemberDto requestDto) throws Exception {
        return memberService.patchMyDetail(requestDto);
    }

    /*
    프로필 사진 수정하기
     */
    @SecurityRequirement(name = "AccessToken")
    @Operation(
            summary = "프로필 사진 수정",
            description = """
            - 입력 가능한 정보:
                - 프로필 사진 이미지
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 사용자 정보 수정",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                                      "success": true,
                                                                      "data": {
                                                                        "id": "gangjjang5",
                                                                        "role": "UNCERTIFIED",
                                                                        "gender": "other",
                                                                        "name": "gangjjang5",
                                                                        "town": "서울특별시",
                                                                        "profile": "추후수정필요 프로필 이미지 고정값",
                                                                        "isAgree": true
                                                                      },
                                                                      "error": null
                                                                    }
                                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "40102",
                            description = "유효하지 않은 계정",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "data": {},
                        "error": {
                            "code": "40102",
                            "message": "유효하지 않은 계정입니다"
                        }
                    }
                    """)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "50000",
                            description = "서버 내부 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "data": {},
                        "error": {
                            "code": "50000",
                            "message": "서버 내부 오류가 발생했습니다"
                        }
                    }
                    """)
                            )
                    )
            }
    )
    @PatchMapping(value = "/profile-img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<ResponseCommonMemberDto>> patchOwnProfileImg(@ModelAttribute RequestPatchProfileImgDto requestDto) throws Exception {
        return memberService.patchMyProfileImg(requestDto);
    }

    @SecurityRequirement(name = "AccessToken")
    @Operation(
            summary = "회원 차단",
            description = "회원 ID로 회원을 차단합니다",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 사용자 차단",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                        "success": true,
                                                        "data": null,
                                                        "error": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "40102",
                            description = "유효하지 않은 계정",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                        "success": false,
                                                        "data": {},
                                                        "error": {
                                                            "code": "40102",
                                                            "message": "유효하지 않은 계정입니다"
                                                        }
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "50000",
                            description = "서버 내부 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                        "success": false,
                                                        "data": {},
                                                        "error": {
                                                            "code": "50000",
                                                            "message": "서버 내부 오류가 발생했습니다"
                                                        }
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping(value = "/block")
    public ResponseEntity<CommonResponse<Object>> blockMember(@RequestParam("memberId") String memberId) throws Exception {
        return memberService.blockMember(memberId);
    }
}