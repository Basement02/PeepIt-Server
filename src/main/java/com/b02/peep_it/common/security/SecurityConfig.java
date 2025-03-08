package com.b02.peep_it.common.security;

import com.b02.peep_it.common.filter.LoggingFilter;
import com.b02.peep_it.common.security.token.AccessTokenFilter;
import com.b02.peep_it.common.security.token.RefreshTokenFilter;
import com.b02.peep_it.common.security.token.RegisterTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private final RegisterTokenFilter registerTokenFilter;
    private final AccessTokenFilter accessTokenFilter;
    private final RefreshTokenFilter refreshTokenFilter;
    private final LoggingFilter loggingFilter;
    private final ApiVersionFilter apiVersionFilter;

    private String[] permitList = {
            "/test/**", "/auth/**", "/api/v1/auth/**", "/api/v2/auth/**",
            "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(HttpBasicConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers(permitList).permitAll()
                            .anyRequest().authenticated();
                })

                /*
                ApiVersionFilter를 인증 필터보다 먼저 실행하면 토큰 검증이 제대로 동작하지 않을 수 있음
                RefreshTokenFilter 다음에 실행되도록 addFilterAfter()를 사용해야 함
                 */
                .addFilterBefore(loggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(registerTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(accessTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(refreshTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(apiVersionFilter, UsernamePasswordAuthenticationFilter.class);

        ;

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}