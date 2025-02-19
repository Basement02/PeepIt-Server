package com.b02.peep_it.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class ApiVersionFilter extends OncePerRequestFilter {

    private static final String API_PREFIX = "/api/v1";

    // ✅ Swagger & test 관련 경로는 필터에서 제외
    private static final String[] SWAGGER_PATHS = {
            "/swagger-ui", "/swagger-ui.html", "/v3/api-docs", "/v3/api-docs/swagger-config",
            "/swagger-resources", "/swagger-resources/**", "/webjars/**",
            "/test"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // ✅ Swagger 관련 요청이면 필터를 타지 않고 그대로 진행
        for (String path : SWAGGER_PATHS) {
            if (requestURI.startsWith(path)) {
                log.info("✅ Swagger 요청 예외 처리: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }
        }

        // ✅ API 요청이 '/api/v1'로 시작하지 않으면 차단
        if (!requestURI.startsWith(API_PREFIX)) {
            log.warn("❌ API 요청이 '/api/v1'로 시작하지 않음: {}", requestURI);
            response.sendError(HttpStatus.FORBIDDEN.value(), "Invalid API Version");
            return;
        }

        // '/api/v1' 제거한 경로 생성
        String newPath = requestURI.substring(API_PREFIX.length());
        if (newPath.isEmpty()) {
            newPath = "/";
        }

        log.info("✅ API 요청 허용: {} → {}", requestURI, newPath);

        // 요청을 새로운 경로로 리다이렉트
        request.getRequestDispatcher(newPath).forward(request, response);
    }
}
