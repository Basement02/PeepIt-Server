package com.b02.peep_it.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) { // 웹소켓 연결
        // 1) 브라우저용 SockJS (fallback)
        registry.addEndpoint("/ws-chat-sockjs").setAllowedOriginPatterns("*").withSockJS();
        // 2) 모바일용 순수 STOMP over WebSocket
        registry.addEndpoint("/ws-chat").setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/pub"); // 입장
//        registry.enableSimpleBroker("/sub"); // 클라이언트에 메세지 전달은 SimpMessagingTemplate.convertAndSend()으로 변경
    }
}
