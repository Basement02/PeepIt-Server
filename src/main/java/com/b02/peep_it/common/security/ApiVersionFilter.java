package com.b02.peep_it.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class ApiVersionFilter extends GenericFilterBean {

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/swagger-ui", "/swagger-ui.html", "/v3/api-docs", "/swagger-resources", "/webjars",
            "/auth", "/test" // 필요한 기타 경로들
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestUri = httpRequest.getRequestURI();

        // Swagger 및 기타 예외 경로는 그대로 처리
        if (EXCLUDED_PATHS.stream().anyMatch(requestUri::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        // API 버전이 없는 경우 자동으로 /api/v1 추가
        if (!requestUri.matches("^/api/v\\d+/.*$")) {
            String newPath = "/api/v1" + requestUri;
            httpRequest.getRequestDispatcher(newPath).forward(request, response);
            return;
        }

        chain.doFilter(request, response);
    }

}
