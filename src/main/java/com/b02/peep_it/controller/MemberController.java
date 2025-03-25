package com.b02.peep_it.controller;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.dto.RequestPatchTownDto;
import com.b02.peep_it.dto.RequestSignUpDto;
import com.b02.peep_it.dto.ResponseLoginDto;
import com.b02.peep_it.dto.member.CommonMemberDto;
import com.b02.peep_it.service.AuthService;
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

    /*
    계정 생성
     */
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
}
