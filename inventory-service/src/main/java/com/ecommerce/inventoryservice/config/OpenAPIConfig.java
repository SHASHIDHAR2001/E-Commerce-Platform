package com.ecommerce.inventoryservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Service API")
                        .version("1.0.0")
                        .description("Inventory Management Microservice - Handles product management, stock control, and inventory operations")
                        .contact(new Contact()
                                .name("E-Commerce Platform")
                                .email("support@ecommerce.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8000").description("Inventory Service")
                ));
    }
}
