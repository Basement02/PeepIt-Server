package com.b02.peep_it.service;

import com.b02.peep_it.domain.Member;
import com.b02.peep_it.dto.ChatReceiveDto;
import com.b02.peep_it.dto.ChatSendDto;
import com.b02.peep_it.repository.MemberRepository;
import com.b02.peep_it.common.util.TimeAgoUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatListener { // RabbitMQ로부터 메시지를 읽어옴

    private final ConnectionFactory connectionFactory;
    private final SimpMessagingTemplate messagingTemplate;
    private final MemberRepository memberRepository;
    private final TimeAgoUtils timeAgoUtils;
    private final ObjectMapper objectMapper;
    private final AmqpAdmin amqpAdmin;

    // 관리 중인 컨테이너 목록
    private final Map<Long, SimpleMessageListenerContainer> containerMap = new ConcurrentHashMap<>();

    public void startListenerForRoom(Long peepId) {
        if (containerMap.containsKey(peepId)) return;

        String queueName = "chat.room." + peepId;

        Queue queue = new Queue(queueName, false, false, true);
        amqpAdmin.declareQueue(queue);

        // 필요한 경우, exchange 바인딩도 여기서 함께
        TopicExchange exchange = new TopicExchange("chat.exchange", true, false);
        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("room." + peepId));

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener((MessageListener) message -> {
            log.info("🐇 MQ에서 메시지 수신");
            try {
                ChatSendDto dto = objectMapper.readValue(message.getBody(), ChatSendDto.class);

                Optional<Member> memberOpt = memberRepository.findById(dto.getUid());
                if (memberOpt.isEmpty()) return;

                ChatReceiveDto responseDto = ChatReceiveDto.builder()
                        .peepId(dto.getPeepId())
                        .nickname(memberOpt.get().getNickname())
                        .imgUrl(memberOpt.get().getProfileImg())
                        .content(dto.getContent())
                        .sendAt(timeAgoUtils.getTimeAgo(dto.getRegDate()))
                        .build();

                messagingTemplate.convertAndSend("/sub/chat.receive." + dto.getPeepId(), responseDto); // 구독자에게 전송
            } catch (Exception e) {
                log.error("메시지 처리 실패", e);
            }
        });

        containerMap.put(peepId, container);
        container.start();
        log.info("✅ 채팅방 {} 큐 리스너 시작", peepId);
    }

    public void stopListenerForRoom(Long peepId) {
        SimpleMessageListenerContainer container = containerMap.remove(peepId);
        if (container != null) {
            container.stop();
            log.info("🛑 채팅방 {} 큐 리스너 종료", peepId);
        }
    }

    @PreDestroy
    public void cleanupAll() {
        containerMap.values().forEach(SimpleMessageListenerContainer::stop);
    }
}
