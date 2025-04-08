package com.b02.peep_it.common.filter;

import com.b02.peep_it.common.util.JwtUtils;
import com.b02.peep_it.domain.Member;
import com.b02.peep_it.dto.member.CommonMemberDto;
import com.b02.peep_it.dto.token.CreateTokenResponseDto;
import com.b02.peep_it.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class RefreshTokenFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            if (jwtUtils.validateRefreshToken(token)) {
                Optional<Member> member = memberRepository.findById(jwtUtils.getClaims(token).get("uid", String.class));
                CommonMemberDto commonMemberDto = CommonMemberDto.builder()
                        .id(member.get().getId())
                        .role(member.get().getRole().toString())
                        .build();
                String newAccessToken = jwtUtils.createAccessToken(commonMemberDto);
                String newRefreshToken = jwtUtils.createRefreshToken(commonMemberDto);
                log.info("Refresh Token validated and new Tokens issued.");

                CreateTokenResponseDto responseDto = CreateTokenResponseDto.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .build();

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(objectMapper.writeValueAsString(responseDto));
                response.getWriter().flush();
                return;
            }
        }
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \" error code custom 필요\"}");
        response.getWriter().flush();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        log.info("should not refresh filter: {}", path);
        return !path.startsWith("/api/v1/auth/refresh"); // 지정 경로 이외의 요청은 통과 시킴
    }
}

