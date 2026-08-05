package com.fraud.engine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SwaggerConfig - OpenAPI 3.0 documentation configuration.
 * 
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 * Access OpenAPI JSON at: http://localhost:8080/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI fraudDetectionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fraud Detection Rule Engine API")
                        .description("Enterprise-grade fraud detection using Strategy Pattern. " +
                                "Evaluates transactions against configurable rules and categorizes " +
                                "them as SAFE, SUSPICIOUS, or FRAUD.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Fraud Engine Team")
                                .email("fraud-engine@enterprise.com"))
                        .license(new License()
                                .name("Enterprise License")
                                .url("https://enterprise.com/licenses")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://api.fraud-engine.prod").description("Production")
                ));
    }
}
