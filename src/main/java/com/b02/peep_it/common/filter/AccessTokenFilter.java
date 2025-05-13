package com.b02.peep_it.common.filter;

import com.b02.peep_it.common.util.CustomUserDetails;
import com.b02.peep_it.common.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@RequiredArgsConstructor
@Component
public class AccessTokenFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            if (jwtUtils.validateAccessToken(token)) {
                log.info("검증 완료된 정상 토큰");
                Authentication authentication = jwtUtils.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Request to {}: JWT(access)={}", request.getRequestURI(), token);
//                CustomUserDetails userDetails = (CustomUserDetails) authentication.getDetails();
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

                log.info("CustomUserDetails 객체 적용됨!");
                log.info("User ID (uid): " + userDetails.getUid());
                log.info("Username (닉네임): " + userDetails.getUsername());
                log.info("Provider (공급자): " + userDetails.getProvider());
                log.info("ProviderId (소셜 고유 ID): " + userDetails.getProviderId());
                log.info("Authorities: " + userDetails.getAuthorities());
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                PrintWriter writer = response.getWriter();

                String json = """
                                {
                                    "success": false,
                                    "data": {},
                                    "error": {
                                        "code": "40300",
                                        "message": "유효하지 않은 토큰입니다"
                                    }
                                }
                            """;

                writer.write(json);
                writer.flush();
                writer.close();
                return;
            }
        }
        else {
            log.info("!!!!!!!!!!Authorization header is missing!!!!!!!!!!!");
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/check")
                || path.startsWith("/api/v1/test")
                || path.startsWith("/api/v1/auth/social")
                ; // auth 경로로 접근하는 모든 요청은 필터 제외
    }
}
