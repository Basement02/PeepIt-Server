package com.b02.peep_it.common.filter;

import com.b02.peep_it.common.model.HttpLogMessage;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.IOException;

public class HttpLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 요청과 응답을 캐싱할 수 있도록 래핑합니다.
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        // 요청 처리 시작 시간 기록
        long startTime = System.currentTimeMillis();

        // 다음 필터 체인 호출
        filterChain.doFilter(wrappedRequest, wrappedResponse);

        // 요청 처리 시간 계산
        double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;

        // HttpLogMessage 생성
        HttpLogMessage logMessage = HttpLogMessage.createInstance(wrappedRequest, wrappedResponse, elapsedTime);

        // 로그 출력 (예: 콘솔에 출력)
        System.out.println(logMessage.toPrettierLog());

        // 응답 바디를 클라이언트에 전달 (ContentCachingResponseWrapper 사용 시 반드시 복원)
        wrappedResponse.copyBodyToResponse();
    }
}
