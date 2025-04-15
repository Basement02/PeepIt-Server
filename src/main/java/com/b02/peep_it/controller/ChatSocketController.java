package com.b02.peep_it.controller;

import com.b02.peep_it.domain.Chat;
import com.b02.peep_it.dto.ChatRequestDto;
import com.b02.peep_it.dto.ChatResponseDto;
import com.b02.peep_it.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;

    @MessageMapping("/chat.send.{peepId}")
    @SendTo("/topic/room/{peepId}")
    public ChatResponseDto sendChat(@DestinationVariable Long peepId,
                                    ChatRequestDto request,
                                    Principal principal) {
        String memberId = principal.getName();
        Chat chat = chatService.save(peepId, memberId, request.content());
        return ChatResponseDto.builder()
                .sender(chat.getMember().getId())
                .content(chat.getContent())
                .sentAt(chat.getCreatedAt())
                .build();
    }
}