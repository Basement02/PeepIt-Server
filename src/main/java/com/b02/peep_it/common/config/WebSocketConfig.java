package com.b02.peep_it.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer { // STOMP 브로커와 WebSocket 경로 연결 (구독)

    @Value("${spring.rabbitmq.host}")
    private String HOST;
    @Value("${spring.rabbitmq.username")
    private String USERNAME;
    @Value("${spring.rabbitmq.password")
    private String PASSWORD;

    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat").setAllowedOriginPatterns("*").withSockJS();
    }

    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/pub");
        registry.enableStompBrokerRelay("/exchange") // 구독 (채팅방 입장)
                .setRelayHost(HOST)
                .setRelayPort(61613)
                .setClientLogin(USERNAME)
                .setClientPasscode(PASSWORD);
    }
}
