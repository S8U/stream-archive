package com.github.s8u.streamarchive.platform.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.JdkClientHttpRequestFactory
import java.net.http.HttpClient
import java.time.Duration

/**
 * 플랫폼 API 클라이언트 설정
 */
@Configuration
class PlatformApiClientConfig {

    @Bean
    fun platformApiClientRequestFactory(): ClientHttpRequestFactory {
        val httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build()

        return JdkClientHttpRequestFactory(httpClient).apply {
            setReadTimeout(RESPONSE_TIMEOUT)
        }
    }

    companion object {
        private val CONNECT_TIMEOUT = Duration.ofSeconds(3)
        private val RESPONSE_TIMEOUT = Duration.ofSeconds(10)
    }

}
