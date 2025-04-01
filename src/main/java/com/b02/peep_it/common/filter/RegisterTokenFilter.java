package com.b02.peep_it.common.filter;

import com.b02.peep_it.common.util.AuthUtils;
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

        // ✅ 1. Authorization 헤더 확인 로그 추가
        if (bearerToken == null) {
            log.warn("❌ Authorization 헤더가 존재하지 않음!");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        log.info("✅ Authorization 헤더 확인됨: {}", bearerToken);

        if (bearerToken.startsWith("Register ")) {
            String token = bearerToken.substring(9).trim(); // ✅ 토큰 앞뒤 공백 제거
            log.info("✅ 추출된 RegisterToken: {}", token);

            // ✅ 2. RegisterToken 유효성 검사
            if (!jwtUtils.validateRegisterToken(token)) {
                log.warn("❌ RegisterToken 검증 실패!");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // ✅ 3. 임시 Authentication 객체 생성
            Authentication authentication = jwtUtils.getTempAuthentication(token);
            if (authentication == null) {
                log.warn("❌ JWT에서 Authentication 객체 생성 실패!");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("✅ SecurityContext에 Authentication 설정 완료!");

            // ✅ 4. AuthUtils에서 Principal 가져오기 (여기서 null이 나오면 문제 발생)
            CustomUserDetails userDetails = authUtils.getPrincipal();
            if (userDetails == null) {
                log.warn("❌ userDetails가 null임! SecurityContext 설정 확인 필요");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            log.info("✅ CustomUserDetails 적용됨!");
            log.info("User ID (uid): {}", userDetails.getUid());
            log.info("Username (닉네임): {}", userDetails.getUsername());
            log.info("Provider (공급자): {}", userDetails.getProvider());
            log.info("ProviderId (소셜 고유 ID): {}", userDetails.getProviderId());
            log.info("Authorities: {}", userDetails.getAuthorities());
        } else {
            log.warn("❌ Authorization 헤더가 'Register '로 시작하지 않음. 값: {}", bearerToken);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/member/sign-up"); // 지정 경로 이외의 요청은 통과 시킴
    }
}