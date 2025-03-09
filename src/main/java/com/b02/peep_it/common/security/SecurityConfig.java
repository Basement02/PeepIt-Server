package com.b02.peep_it.common.security;

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

    private String[] permitList = {
            "/api/v1/test/**", // /deploy, /health-check, /upload
            "/api/v1/auth/**", // /social, /check/id, /check/phone, /send/sms-code
            "/api/v1/member/**", // /sign-up
            "/api/v1/peep/**", // /post, /get/{peepId}, /my/upload, /my/react, /my/chat, /my/active, /get, /get/hot, /get/town, /get/map
            "/swagger",
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
                .addFilterBefore(registerTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(accessTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(refreshTokenFilter, UsernamePasswordAuthenticationFilter.class)

        ;

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}