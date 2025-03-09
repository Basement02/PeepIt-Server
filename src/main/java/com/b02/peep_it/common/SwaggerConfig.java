package com.b02.peep_it.common;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        String bearerToken = "BearerAuth";
        String registerToken = "RegisterAuth";

        // SecurityRequirement 추가
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(bearerToken)
                .addList(registerToken);

        // SecurityScheme 추가 (각 토큰을 개별적으로 정의)
        Components components = new Components()
                .addSecuritySchemes(bearerToken, new SecurityScheme()
                        .name(bearerToken)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("사용 예시: `Authorization: Bearer JWT_TOKEN`"))
                .addSecuritySchemes(registerToken, new SecurityScheme()
                        .name(registerToken)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("Register")
                        .description("사용 예시: `Authorization: Register REGISTER_TOKEN`"));

        return new OpenAPI()
                .components(components)
                .info(apiInfo())
                .addSecurityItem(securityRequirement)
                // ✅ 기본 API 버전을 `/api/v1`로 설정 (Swagger UI에서 자동 반영)
                .servers(List.of(
                        new Server().url("https://basement02.site").description("base domain")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("API Test")
                .description("""
                        인증 방식:
                        - `Bearer JWT_TOKEN` (access/refresh 토큰)
                        - `Register REGISTER_TOKEN` (회원가입 전용 토큰)
                        
                        API 기본 경로:
                        - `/api/v1/...` (기본 버전)
                        - `/api/v2/...` (향후 지원 예정)
                        """)
                .version("1.0.0");
    }
}