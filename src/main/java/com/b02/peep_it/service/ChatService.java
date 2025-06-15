package com.b02.peep_it.service;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.util.TimeAgoUtils;
import com.b02.peep_it.domain.Chat;
import com.b02.peep_it.domain.Member;
import com.b02.peep_it.domain.Peep;
import com.b02.peep_it.dto.ChatResponseDto;
import com.b02.peep_it.repository.ChatRepository;
import com.b02.peep_it.repository.MemberRepository;
import com.b02.peep_it.repository.PeepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final TimeAgoUtils timeAgoUtils;
    private final ChatRepository chatRepository;
    private final MemberRepository memberRepository;
    private final PeepRepository peepRepository;
    private final AmqpAdmin amqpAdmin;
    private final ChatListener chatListener;

    public Chat save(Long peepId, String memberId, String content) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Peep peep = peepRepository.findById(peepId).orElseThrow();
        Chat chat = Chat.builder()
                .content(content)
                .peep(peep)
                .member(member)
                .build();
        return chatRepository.save(chat);
    }

    public ResponseEntity<CommonResponse<List<ChatResponseDto>>> getChatsByPeepId(Long peepId) {
        List<ChatResponseDto> chatList = chatRepository.findByPeepIdOrderByCreatedAtAsc(peepId)
                .stream()
                .map(chat -> ChatResponseDto.builder()
                        .uid(chat.getMember().getId())
                        .nickname(chat.getMember().getNickname())
                        .imgUrl(chat.getMember().getProfileImg())
                        .content(chat.getContent())
                        .sentAt(timeAgoUtils.getTimeAgo(chat.getCreatedAt()))
                        .build())
                .toList();

        return CommonResponse.ok(chatList);
    }

    public void createRoom(Long peepId) {
        String queueName = "chat.room." + peepId;
        String routingKey = "room." + peepId;
        String exchangeName = "chat.exchange";

        // 1. AMQP 리소스 선언
        Queue queue = new Queue(queueName, true, false, true);
        TopicExchange exchange = new TopicExchange(exchangeName);
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);

        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareBinding(binding);

        // 2. 동적 리스너 등록
        chatListener.startListenerForRoom(peepId);
    }
}