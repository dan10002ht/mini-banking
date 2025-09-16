package com.minibanking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDoc OpenAPI Configuration
 * Provides API documentation for Mini Banking System
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mini Banking System API")
                        .description("Core Banking System with PostgreSQL and Blockchain Integration")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Mini Banking Team")
                                .email("support@minibanking.com")
                                .url("https://minibanking.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.minibanking.com")
                                .description("Production Server")
                ));
    }
}
