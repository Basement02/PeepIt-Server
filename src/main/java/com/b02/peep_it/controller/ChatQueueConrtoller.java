package com.b02.peep_it.controller;

import com.b02.peep_it.common.util.TimeAgoUtils;
import com.b02.peep_it.domain.Member;
import com.b02.peep_it.dto.ChatReceiveDto;
import com.b02.peep_it.dto.ChatSendDto;
import com.b02.peep_it.repository.MemberRepository;
import com.b02.peep_it.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatQueueConrtoller {
    private final RabbitTemplate rabbitTemplate;
    private final ChatService chatService;
    private final MemberRepository memberRepository;
    private final TimeAgoUtils timeAgoUtils;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("chat.send.{peepId}")
    public void send(@DestinationVariable Long peepId, ChatSendDto requestDto) {
        requestDto.setRegDate(LocalDateTime.now());
        chatService.save(peepId, requestDto.getUid(), requestDto.getContent());
        // RabbitMQ는 메시지를 각 구독자 큐에 push
        rabbitTemplate.convertAndSend("chat.exchange", "room." + peepId, requestDto);
    }

    // Spring의 @RabbitListener가 메시지를 수신
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "", durable = "false", exclusive = "true", autoDelete = "true"),
            exchange = @Exchange(value = "chat.exchange", type = "topic"),
            key = "room.*"
    ))
    public void handleMessage(ChatSendDto requestDto) {
        Optional<Member> memberOptional = memberRepository.findById(requestDto.getUid());
        if (memberOptional.isEmpty()) {
            log.info("메세지를 보낸 member 부재");
        }
        Member member = memberOptional.get();

        ChatReceiveDto responseDto = ChatReceiveDto.builder()
                .peepId(requestDto.getPeepId())
                .nickname(member.getNickname())
                .imgUrl(member.getProfileImg())
                .content(requestDto.getContent())
                .sendAt(timeAgoUtils.getTimeAgo(requestDto.getRegDate()))
                .build();

        messagingTemplate.convertAndSend("/sub/chat.room." + requestDto.getPeepId(), responseDto);

//        // STOMP 구독 경로(/exchange/chat.exchange/room.1)로 메시지를 브로드캐스트
//        rabbitTemplate.convertAndSend("/exchange/chat.exchange/room." + requestDto.getPeepId(), responseDto);
    }

}