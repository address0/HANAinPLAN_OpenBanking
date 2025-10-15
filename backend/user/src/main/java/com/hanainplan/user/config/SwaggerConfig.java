package com.hanainplan.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Server API")
                        .description("사용자 관리 및 실명인증 CI 변환 API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("HANAinPLAN Team")
                                .email("support@hanainplan.com")
                                .url("https://hanainplan.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8084")
                                .description("개발 서버"),
                        new Server()
                                .url("http://localhost:8084")
                                .description("로컬 서버")
                ));
    }
}