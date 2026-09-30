package com.app.fisiotech.auth.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiSecurityConfig {
    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
