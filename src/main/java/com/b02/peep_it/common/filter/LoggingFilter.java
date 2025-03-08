package com.b02.peep_it.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(1)    // ✅ 필터 순서 설정 (Spring Security보다 먼저 실행)
@WebFilter("/*")
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);

        long startTime = System.currentTimeMillis();
        chain.doFilter(requestWrapper, responseWrapper);
        long endTime = System.currentTimeMillis();

        // 요청 데이터 추출
        String httpMethod = requestWrapper.getMethod();
        String requestUri = requestWrapper.getRequestURI();
        String authorizationHeader = requestWrapper.getHeader("Authorization");
        String requestBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);

        // Request Parameter 추출 (쿼리 스트링 값)
        Map<String, String[]> parameterMap = requestWrapper.getParameterMap();
        String requestParams = parameterMap.isEmpty() ? "null" :
                parameterMap.entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
                        .collect(Collectors.joining(", "));

        // URL 자체를 로깅
        String pathVariables = requestUri.replaceAll("\\d+", "{id}"); // 숫자 값을 {id}로 변환 (예: /users/123 -> /users/{id})

        // ✅ 로그 출력 (필요한 정보만)
        log.info("""
                [REQUEST] {} {} (PathVariable 변환: {})
                >> Authorization: {}
                >> REQUEST_PARAM: {}
                >> REQUEST_BODY: {}
                """,
                httpMethod, requestUri, pathVariables,
                (authorizationHeader != null ? authorizationHeader : "null"),
                requestParams,
                (!requestBody.isEmpty() ? requestBody : "null")
        );

        responseWrapper.copyBodyToResponse();
    }
}