package com.b02.peep_it.common.security;

import com.b02.peep_it.common.HttpLoggingFilter;
import com.b02.peep_it.common.security.token.AccessTokenFilter;
import com.b02.peep_it.common.security.token.RefreshTokenFilter;
import com.b02.peep_it.common.security.token.RegisterTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
                .cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfigurationSource()))
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(List.of("http://localhost:8080", "http://basement02.site", "https://basement02.site", "http://43.200.44.121:8080", "https://43.200.44.121:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setMaxAge(3600L); // preflight 결과를 캐시하는 시간(초)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public FilterRegistrationBean<HttpLoggingFilter> loggingFilter() {
        FilterRegistrationBean<HttpLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new HttpLoggingFilter());
        registrationBean.addUrlPatterns("/*"); // 모든 URL에 대해 적용
        registrationBean.setOrder(1); // 필터 순서 지정 (낮은 값이 우선)
        return registrationBean;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}