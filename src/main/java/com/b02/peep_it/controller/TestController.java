package com.b02.peep_it.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {
    /*
    배포 테스트
     */
    @GetMapping("/deploy")
    public String checkdeploy() {
        return "Hello World!";
    }
}