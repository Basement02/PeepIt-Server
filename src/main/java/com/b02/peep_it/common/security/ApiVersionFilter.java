package com.b02.peep_it.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
@Component
public class ApiVersionFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestUri = httpRequest.getRequestURI();

        // API 버전이 없는 경우 기본적으로 v1 적용
        if (!requestUri.matches("^/api/v\\d+/.*$")) {
            request.getRequestDispatcher("/api/v1" + requestUri).forward(request, response);
            return;
        }

        chain.doFilter(request, response);
    }
}
