package com.carlos.challenge.infrastructure.config;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.security.SecurityRequirement;

@Configuration
public class OpenApiGlobalSecurityCustomizer {

    @Bean
    OpenApiCustomizer securityOpenApiCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, item) -> {
            // Excluir públicos
            boolean isPublic = path.startsWith("/auth/token")
                    || path.startsWith("/v3/api-docs")
                    || path.startsWith("/swagger-ui")
                    || path.startsWith("/actuator/health")
                    || path.startsWith("/actuator/info");

            if (!isPublic) {
                item.readOperations().forEach(op ->
                        op.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                );
            }
        });
    }
}
