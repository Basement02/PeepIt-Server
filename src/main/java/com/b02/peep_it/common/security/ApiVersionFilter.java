package com.b02.peep_it.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.List;

public class ApiVersionFilter extends GenericFilterBean {

    // Swagger 및 기타 예외 경로를 명시
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/swagger-ui", "/swagger-ui.html", "/v3/api-docs", "/swagger-resources", "/webjars",
            "/auth", "/test"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestUri = httpRequest.getRequestURI();

        // EXCLUDED_PATHS에 해당하는 경우 버전 재작성 없이 그대로 처리
        for (String excluded : EXCLUDED_PATHS) {
            if (requestUri.startsWith(excluded)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // API 버전이 없는 경우 기본적으로 /api/v1 추가
        if (!requestUri.matches("^/api/v\\d+/.*$")) {
            String newPath = "/api/v1" + requestUri;
            // 재작성된 URL로 포워딩
            httpRequest.getRequestDispatcher(newPath).forward(request, response);
            return;
        }

        chain.doFilter(request, response);
    }
}