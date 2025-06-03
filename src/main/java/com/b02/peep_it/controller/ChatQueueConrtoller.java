package com.b02.peep_it.controller;

import com.b02.peep_it.dto.ChatSendDto;
import com.b02.peep_it.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatQueueConrtoller {
    private final RabbitTemplate rabbitTemplate;
    private final ChatService chatService;

    @MessageMapping("chat.send.{peepId}")
    public void send(@DestinationVariable Long peepId, ChatSendDto requestDto) {
        requestDto.setRegDate(LocalDateTime.now());
        chatService.save(peepId, requestDto.getUid(), requestDto.getContent());
        // RabbitMQ는 메시지를 rabbit mq에 전송
        rabbitTemplate.convertAndSend("chat.exchange", "room." + peepId, requestDto);
    }
}