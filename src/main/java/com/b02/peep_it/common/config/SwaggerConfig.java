package com.b02.peep_it.common.config;

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
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("AuthToken", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("API 호출 시 필요한 인증 토큰. 사용 예시: `Authorization: Bearer {JWT_ACCESS_TOKEN}` 또는 `Authorization: Register {REGISTER_TOKEN}`"))
                )
                .addSecurityItem(new SecurityRequirement().addList("AuthToken"))
                .info(new Info()
                        .title("Peep-It API")
                        .version("1.0")
                        .description("핍잇 서비스 API 문서"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("로컬 서버"),
                        new Server().url("https://basement02.site").description("배포 서버")
                ));
    }
}