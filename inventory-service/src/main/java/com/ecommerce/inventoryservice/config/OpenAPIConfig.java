package com.ecommerce.inventoryservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Value("${spring.application.name:Inventory Service}")
    private String appName;

    @Value("${application.version:1.0.0}")
    private String appVersion;

    @Value("${application.description:API documentation}")
    private String appDescription;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title(appName)
                                .version(appVersion)
                                .description(appDescription)
                                .contact(new Contact()
                                        .name("E-Commerce Platform")
                                        .email("support@ecommerce.com"))
                );
    }


    @Bean
    public OpenApiCustomizer dynamicServerCustomizer() {
        return openApi -> openApi.getServers().clear();
    }
}
