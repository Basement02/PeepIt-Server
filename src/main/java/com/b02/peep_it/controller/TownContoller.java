package com.b02.peep_it.controller;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.response.PagedResponse;
import com.b02.peep_it.dto.CommonTownDto;
import com.b02.peep_it.service.TownService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/town")
public class TownContoller {
    private final TownService townService;

    @Operation(summary = "법정동 리스트 조회", description = "서버에 저장된 법정동 정보를 리스트로 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<CommonResponse<PagedResponse<CommonTownDto>>> getStateList(@RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "1000") int size) {
        return townService.getStateListInfo(page, size);
    }

    @Operation(summary = "법정동 코드 조회", description = "법정동 코드로 법정동 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "법정동 조회 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "40403", description = "존재하지 않는 법정동 코드입니다.",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/info")
    public ResponseEntity<CommonResponse<CommonTownDto>> getStateInfo(@RequestParam String legalCode) {
        return townService.getStateInfo(legalCode);

    }
}
