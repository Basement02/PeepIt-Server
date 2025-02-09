package com.b02.peep_it.controller;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.dto.RequestSignUpDto;
import com.b02.peep_it.dto.RequestSocialLoginDto;
import com.b02.peep_it.dto.ResponseLoginDto;
import com.b02.peep_it.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthContoller {
    private final AuthService authService;

    /*
    소셜 로그인
    - 최초: register token 발급
    - 재: access/refresh token 발급
     */
    @PostMapping("/social")
    public ResponseEntity<CommonResponse<ResponseLoginDto>> socialLogin(@RequestBody RequestSocialLoginDto requestDto) {
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
    계정 생성
     */
    @PostMapping("/sign-up")
    public ResponseEntity<CommonResponse<ResponseLoginDto>> signUp(@RequestBody RequestSignUpDto requestDto){
        return authService.createAccount(requestDto);
    }
}