package com.github.s8u.streamarchive.global.config

import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.core.jackson.TypeNameResolver
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.providers.ObjectMapperProvider
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

    /**
     * 스키마 이름 규칙을 바꾼 ModelResolver
     *
     * 안쪽 클래스는 바깥 클래스 이름을 앞에 붙인다 (VideoGetResponse.ChannelInfo -> VideoGetResponseChannelInfo).
     * 기본 규칙은 클래스 이름만 써서, 여러 DTO에 같은 이름의 안쪽 클래스가 있으면 한 스키마로 합쳐진다.
     */
    @Bean
    fun modelResolver(objectMapperProvider: ObjectMapperProvider): ModelResolver {
        val typeNameResolver = object : TypeNameResolver() {
            override fun getNameOfClass(cls: Class<*>): String {
                val enclosingClass = cls.enclosingClass ?: return cls.simpleName
                return getNameOfClass(enclosingClass) + cls.simpleName
            }
        }

        // springdoc이 기본으로 등록한 ModelResolver를 이 빈으로 바꿔 끼운다.
        return ModelResolver(objectMapperProvider.jsonMapper(), typeNameResolver)
    }

}
