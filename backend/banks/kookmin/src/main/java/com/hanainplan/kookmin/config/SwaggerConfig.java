package com.hanainplan.kookmin.config;

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
    public OpenAPI kookminBankOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8083");
        devServer.setDescription("Kookmin Bank Server URL in Development environment");

        Contact contact = new Contact();
        contact.setEmail("kookmin@hanainplan.com");
        contact.setName("Kookmin Bank Team");
        contact.setUrl("https://www.hanainplan.com");

        License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Kookmin Bank API")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints for Kookmin Bank services including customer management, account management, and product subscriptions.")
                .termsOfService("https://www.hanainplan.com/terms")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}