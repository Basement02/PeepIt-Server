package com.b02.peep_it.common.filter;

import com.b02.peep_it.common.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String bearerToken = servletRequest.getServletRequest().getHeader("Authorization");

            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                String token = bearerToken.substring(7);
                if (jwtUtils.validateAccessToken(token)) {
                    String memberId = jwtUtils.getUidfromToken(token);
                    log.info("WebSocket 연결 인증 완료 - memberId: {}", memberId);
                    attributes.put("memberId", memberId);
                    return true;
                } else {
                    log.warn("WebSocket 토큰 검증 실패");
                }
            } else {
                log.warn("WebSocket Authorization 헤더 누락 또는 형식 오류");
            }
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception ex) {
        // 연결 이후 추가 작업 필요시 작성
    }
}