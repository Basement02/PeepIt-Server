package com.b02.peep_it.controller;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.dto.RequestPatchTownDto;
import com.b02.peep_it.dto.RequestSignUpDto;
import com.b02.peep_it.dto.ResponseLoginDto;
import com.b02.peep_it.dto.member.CommonMemberDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<CommonResponse<ResponseLoginDto>> signUp(@RequestBody RequestSignUpDto requestDto){
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
    public ResponseEntity<CommonResponse<CommonMemberDto>> patchTown(@RequestBody RequestPatchTownDto requestDto) {
        log.info("요청받은 DTO: {}", requestDto);
        return townService.updateTownInfo(requestDto);
    }

    /*
    아이디로 멤버 정보 조회
     */
    @SecurityRequirement(name = "AccessToken")
    @Operation(
            summary = "멤버 프로필 조회하기 (아이디/닉네임/동네/프로필이미지)",
            description = """
            - 입력 가능한 정보:
                - memberId: 사용자 고유 ID
            - 역할 상세
                 - CERT("CERTIFIED", "인증 회원"),
                 - UNCERT("UNCERTIFIED", "미인증 회원"),
                 - MAN("MANAGER", "관리자"),
                 - DEV("DEVELOPER", "개발자")
            """,
            security = {
                    @SecurityRequirement(name = "AuthToken")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "멤버 ID",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "멤버 프로필 조회 예시",
                                            value = """
                                            {
                                                "memberId": "1234512345"
                                            }
                                    """)
                            }
                    )
            ),
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
                                                                        "name": "gangjjang5",
                                                                        "town": "서울특별시",
                                                                        "profile": "추후수정필요 프로필 이미지 고정값"
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
                            responseCode = "40304",
                            description = "존재하지 않는 사용자",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "data": {},
                        "error": {
                            "code": "40404",
                            "message": "존재하지 않는 사용자입니다"
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
    @GetMapping("/detail/{memberId}")
    public ResponseEntity<CommonResponse<CommonMemberDto>> getMemberDetail(@PathVariable("memberId") String memberId) {
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
            security = {
                    @SecurityRequirement(name = "AuthToken")
            },
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
                                                                        "name": "gangjjang5",
                                                                        "town": "서울특별시",
                                                                        "profile": "추후수정필요 프로필 이미지 고정값"
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
    public ResponseEntity<CommonResponse<CommonMemberDto>> getOwnDetail() {
        return memberService.getMyDetail();
    }
}
