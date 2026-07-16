package com.membership.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger UI configuration.
 * Accessible at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI membershipServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Membership Service API")
                        .description("""
                                Production-ready membership management service.
                                
                                Features:
                                - Configurable membership plans (Monthly, Quarterly, Yearly)
                                - Tiered membership system (Silver, Gold, Platinum) with extensible benefits
                                - Strategy Pattern-based tier evaluation engine
                                - Optimistic locking for concurrent request safety
                                - Full audit history for all membership state transitions
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Membership Service Team")
                                .email("membership@example.com"))
                        .license(new License()
                                .name("MIT License")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("http://membership-service:8080").description("Docker")
                ));
    }
}
