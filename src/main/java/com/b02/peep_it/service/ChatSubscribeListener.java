package com.b02.peep_it.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSubscribeListener {

    private final ChatListener chatListener;

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String destination = accessor.getDestination(); // ex: "/sub/chat.receive.123"
        if (destination == null || !destination.startsWith("/sub/chat.receive.")) return;

        try {
            String peepIdStr = destination.replace("/sub/chat.receive.", "");
            Long peepId = Long.parseLong(peepIdStr);

            chatListener.startListenerForRoom(peepId);
            log.info("👂 MQ 리스너 자동 등록: Peep {}", peepId);

        } catch (Exception e) {
            log.warn("❌ 구독 경로 파싱 실패: {}", destination, e);
        }
    }
}

