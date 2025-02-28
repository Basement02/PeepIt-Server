package com.b02.peep_it.common.security.token;

import com.b02.peep_it.common.util.AuthUtils;
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

@Slf4j
@RequiredArgsConstructor
@Component
public class RegisterTokenFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final AuthUtils authUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Register ")) {
            String token = bearerToken.substring(9);
            if (jwtUtils.validateRegisterToken(token)) {
                Authentication authentication = jwtUtils.getTempAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                CustomUserDetails userDetails = authUtils.getPrincipal();
                log.info("Request to {}: REGISTER(access)={}", request.getRequestURI(), token);
                log.info("✅CustomUserDetails 객체 적용됨!");
                log.info("User ID (uid): " + userDetails.getUid());
                log.info("Username (닉네임): " + userDetails.getUsername());
                log.info("Provider (공급자): " + userDetails.getProvider());
                log.info("ProviderId (소셜 고유 ID): " + userDetails.getProviderId());
                log.info("Authorities: " + userDetails.getAuthorities());
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/auth/sign-up"); // 지정 경로 이외의 요청은 통과 시킴
    }
}
