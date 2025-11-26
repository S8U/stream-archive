package com.github.s8u.streamarchive.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "Bearer Authentication"

        return OpenAPI()
            .info(
                Info()
                    .title("Stream Archive API")
                    .description("멀티 플랫폼 스트리밍 녹화 시스템 API")
                    .version("1.0.0")
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT 액세스 토큰을 입력하세요")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
    }
}
