//package com.ecommerce.orderservice.config;
//
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.info.Contact;
//import io.swagger.v3.oas.models.info.Info;
//import io.swagger.v3.oas.models.servers.Server;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.List;
//
//@Configuration
//public class OpenAPIConfig {
//
//    @Bean
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .info(new Info()
//                        .title("Order Service API")
//                        .version("1.0.0")
//                        .description("Order Management Microservice - Handles order placement, status updates, and order retrieval")
//                        .contact(new Contact()
//                                .name("E-Commerce Platform")
//                                .email("support@ecommerce.com")))
//                .servers(List.of(
//                        new Server().url("http://localhost:8080").description("Order Service")
//                ));
//    }
//}

package com.ecommerce.orderservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Value("${spring.application.name:Order Service}")
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

    /**
     * This ensures the server URL becomes the actual URL
     * that the request is coming from, instead of any hardcoded value.
     */
    @Bean
    public OpenApiCustomizer dynamicServerCustomizer() {
        return openApi -> openApi.getServers().clear();
        // Swagger UI will automatically use current request URL
    }
}
