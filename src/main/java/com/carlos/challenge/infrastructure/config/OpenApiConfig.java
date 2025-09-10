package com.carlos.challenge.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    public static final String API = "API";
    public static final String VERSION = "1.0";
    public static final String BASIC_AUTH = "basicAuth";
    public static final String BASIC = "basic";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title(API).version(VERSION))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(BASIC_AUTH,
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme(BASIC)));
    }
}
