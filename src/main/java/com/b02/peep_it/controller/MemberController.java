package com.b02.peep_it.controller;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.dto.RequestSignUpDto;
import com.b02.peep_it.dto.ResponseLoginDto;
import com.b02.peep_it.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/member")
public class MemberController {
    private final AuthService authService;
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
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(
                            name = "Authorization",
                            description = "Register 형식의 JWT 액세스 토큰",
                            required = true,
                            example = "Register eyJhbGciOiJIUzI1NiIs토큰토큰In..."
                    )
            },
            requestBody = @RequestBody(
                    description = "계정 생성 요청 DTO",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "계정 생성 요청 예시",
                                            value = """
                        {
                            "id": "핍잇에서-사용할-id",
                            "nickname": "핍잇에서-사용할-nickname",
                            "birth": "",
                            "gender": "",
                            "isAgree": "true"
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
        return authService.createAccount(requestDto);
    }
}
