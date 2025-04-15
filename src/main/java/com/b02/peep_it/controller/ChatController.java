package com.b02.peep_it.controller;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.dto.ChatResponseDto;
import com.b02.peep_it.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    @SecurityRequirement(name = "AccessToken")
    @Operation(summary = "특정 핍의 채팅 메시지 조회", description = "해당 Peep(게시물)에 대한 전체 채팅 메시지를 시간순으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 Peep ID가 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class)))
    })
    @GetMapping("/{peepId}")
    public ResponseEntity<CommonResponse<List<ChatResponseDto>>> getChatHistory(@PathVariable Long peepId) {
        return chatService.getChatsByPeepId(peepId);
    }
}