package com.hanainplan.hanhwa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI hanhwaInsuranceOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8092");
        devServer.setDescription("Hanhwa Insurance Server URL in Development environment");

        Contact contact = new Contact();
        contact.setEmail("hanhwa-insurance@hanainplan.com");
        contact.setName("Hanhwa Insurance Team");
        contact.setUrl("https://www.hanainplan.com");

        License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Hanhwa Insurance API")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints for Hanhwa Insurance services including insurance product management, customer management, and policy subscriptions.")
                .termsOfService("https://www.hanainplan.com/terms")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}
