package com.b02.peep_it.controller;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.s3.S3Utils;
import com.b02.peep_it.dto.RequestSignUpDto;
import com.b02.peep_it.dto.ResponseLoginDto;
import com.b02.peep_it.service.TestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/test")
public class TestController {
    private final S3Utils s3Utils;
    private final TestService testService;

    /*
    배포 테스트
     */
    @GetMapping("/deploy")
    public String checkdeploy() {
        return "Hello World!";
    }

    /*
    https health check
     */
    @GetMapping("/health-check")
    public String healthcheck() {
        return "OK";
    }

    /*
    s3 upload
     */
    @PostMapping("/upload")
    public String uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = s3Utils.uploadFile(file);
            return imageUrl;
        } catch (IOException e) {
            return "ERROR! 이미지 업로드 실패";
        }
    }

    /*
    테스트 사용자 토큰 발급
     */
    @PostMapping("/giver/test")
    public ResponseEntity<CommonResponse<ResponseLoginDto>> getGiverToken(@RequestBody RequestSignUpDto requestDto) {
        log.info("입력된 값 -> {}", requestDto);
        return testService.createAccount(requestDto);
    }
}