package com.saarthi.saarthi_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI saarthiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SAARTHI API Engine")
                        .description("Automated Organ Donation Registry & Allocation Engine")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("SAARTHI Development Team")
                                .email("contact@saarthi.local")));
    }
}