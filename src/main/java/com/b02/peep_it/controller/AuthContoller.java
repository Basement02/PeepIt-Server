package com.b02.peep_it.controller;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.dto.RequestSocialLoginDto;
import com.b02.peep_it.dto.ResponseLoginDto;
import com.b02.peep_it.dto.member.ResponseCommonMemberDto;
import com.b02.peep_it.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthContoller {
    private final AuthService authService;

    /*
    소셜 로그인
    - 최초: register token 발급
    - 재: access/refresh token 발급
    KAKAO("KAKAO", "카카오 인증 회원"),
    NAVER("NAVER", "네이버 인증 회원"),
    APPLE("APPLE", "애플 인증 회원"),
    TESTER("TESTER", "테스터: 가상 회원")
     */
    @Operation(
            summary = "소셜 로그인",
            description = """
            - 최초 로그인 시 register token 발급
            - 기존 회원 로그인 시 access/refresh token 발급
            - 지원하는 provider:
              - KAKAO: 카카오 인증 회원
              - NAVER: 네이버 인증 회원
              - APPLE: 애플 인증 회원
              - TESTER: 테스트 계정
        """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "소셜 로그인 요청 DTO",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "카카오 로그인 요청 예시",
                                            value = """
                        {
                            "provider": "KAKAO",
                            "idToken": "kakao-id-token-sample"
                        }
                        """),
                                    @ExampleObject(
                                            name = "테스터 로그인 요청 예시",
                                            value = """
                        {
                            "provider": "TESTER",
                            "idToken": "gangjjang5"
                        }
                        """)
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "성공적으로 로그인 또는 회원가입 진행됨",
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
    @PostMapping("/social")
    public ResponseEntity<CommonResponse<ResponseLoginDto>> socialLogin(@RequestBody RequestSocialLoginDto requestDto) throws Exception {
        log.info("==== provider = [{}] ===", requestDto.provider());
        log.info("provider: {}", requestDto.provider());
        log.info("idToken: {}", requestDto.idToken());

        return authService.getRegisterToken(requestDto);
    }

    /*
    아이디 중복 확인
     */
    @GetMapping("/check/id")
    public ResponseEntity<CommonResponse<Object>> checkId(@RequestParam("id") String id){
        return authService.isIdDuplicated(id);
    }

    /*
    전화번호 중복 확인
     */
    @GetMapping("/check/phone")
    public ResponseEntity<CommonResponse<Object>> checkPhone(@RequestParam("phone") String phone){
        return authService.isPhoneDuplicated(phone);
    }

    /*
    전화번호 인증코드 발급
     */
    @SecurityRequirement(name = "AccessToken")
    @PostMapping("/send/sms-code")
    public ResponseEntity<CommonResponse<String>> sendSmsCode(@RequestParam("phone") String phone) throws CoolsmsException {
        log.info("==== phone = [{}] ===", phone);
        return authService.sendSmsCode(phone);
    }

    /*
    전화번호 인증코드 검증
     */
    @SecurityRequirement(name = "AccessToken")
    @Operation(
            summary = "전화번호 인증코드 검증",
            description = """
            - 입력 가능한 정보:
                - 전화번호 인증코드
                - 전화번호
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
                    )
            }
    )
    @PostMapping("/verify/sms-code")
    public ResponseEntity<CommonResponse<ResponseCommonMemberDto>> verifySmsCode(
            @RequestParam("phone") String phone,
            @RequestParam("code") String inputCode) throws CoolsmsException {
        log.info("==== phone = [{}] ===", phone);
        log.info("code = [{}]", inputCode);
        return authService.verifySmsCode(phone, inputCode);
    }

    /*
    로그아웃
     */
    @SecurityRequirement(name = "AccessToken")
    @Operation(
            summary = "로그아웃",
            description = """
            - 입력 가능한 정보:
                - 없음
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그아웃 성공",
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
                    )
            }
    )
    @DeleteMapping("/logout")
    public ResponseEntity<CommonResponse<Object>> logout(@Parameter(hidden = true) @RequestHeader("Authorization") String bearerToken) {
        return authService.logout(bearerToken);
    }
}