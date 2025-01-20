package com.b02.peep_it.security.token;

import com.b02.peep_it.domain.Member;
import com.b02.peep_it.dto.member.MemberDto;
import com.b02.peep_it.dto.token.ReTokenResponseDto;
import com.b02.peep_it.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                MemberDto memberDto = MemberDto.builder()
                        .id(member.get().getId())
                        .role(member.get().getRole())
                        .build();
                String newAccessToken = jwtUtils.createAccessToken(memberDto);
                String newRefreshToken = jwtUtils.createRefreshToken(memberDto);
                log.info("Refresh Token validated and new Tokens issued.");

                ReTokenResponseDto responseDto = ReTokenResponseDto.builder()
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
        return !path.startsWith("/auth/refresh"); // 지정 경로 이외의 요청은 통과 시킴
    }
}

