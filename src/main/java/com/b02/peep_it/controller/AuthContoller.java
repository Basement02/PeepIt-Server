package com.b02.peep_it.controller;

import com.b02.peep_it.common.ApiResponse;
import com.b02.peep_it.dto.RequestSignUpDto;
import com.b02.peep_it.dto.RequestSocialLoginDto;
import com.b02.peep_it.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthContoller {
    private final AuthService authService;

    /*
    register token 발급
     */
    @PostMapping("/social")
    public ApiResponse<Object> socialLogin(@RequestBody RequestSocialLoginDto requstDto) {
        return authService.getRegisterToken(requstDto);
    }

    /*
    아이디 중복 확인
     */
    @GetMapping("/check/id")
    public ApiResponse<Object> checkId(@RequestParam("id") String id){
        return authService.isIdDuplicated(id);
    }

    /*
    전화번호 중복 확인
     */
    @GetMapping("/check/phone")
    public ApiResponse<Object> checkPhone(@RequestParam("phone") String phone){
        return authService.isPhoneDuplicated(phone);
    }

    /*
    계정 등록
     */
//    @PostMapping("/signUp")
//    public ApiResponse<Object> signUp(@RequestBody RequestSignUpDto requestDto) {
//        return authService.createAccount(requestDto);
//    }
}