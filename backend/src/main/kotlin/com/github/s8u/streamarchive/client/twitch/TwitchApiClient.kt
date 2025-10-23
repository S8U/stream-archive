package com.github.s8u.streamarchive.client.twitch

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.s8u.streamarchive.properties.TwitchProperties
import org.slf4j.LoggerFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class TwitchApiClient(
    private val twitchProperties: TwitchProperties
) {

    private val logger = LoggerFactory.getLogger(TwitchApiClient::class.java)

    private var appOauthToken: String? = null

    private val restClient: RestClient = RestClient.builder()
        .messageConverters { converters ->
            val objectMapper = ObjectMapper().apply {
                registerKotlinModule()
                propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
            converters.removeIf { it is MappingJackson2HttpMessageConverter }
            converters.add(MappingJackson2HttpMessageConverter(objectMapper))
        }
        .build()

    /**
     * OAuth Token 생성
     * https://dev.twitch.tv/docs/api/get-started/
     */
    fun createOauthToken(): TwitchOauthResponseDto? {
        return restClient.post()
            .uri("https://id.twitch.tv/oauth2/token")
            .header("client_id", twitchProperties.appClientId)
            .header("client_secret", twitchProperties.appClientSecret)
            .header("grant_type", "client_credentials")
            .retrieve()
            .body(TwitchOauthResponseDto::class.java)
    }

    /**
     * OAuth Token 재발급
     */
    fun refreshOauthToken(): String? {
        appOauthToken = createOauthToken()?.accessToken
        return appOauthToken
    }

    /**
     * 트위치 유저 정보 불러오기
     * https://dev.twitch.tv/docs/api/reference/#get-users
     */
    fun getUsers(request: TwitchUsersRequestDto): TwitchUsersResponseDto? {
        return executeWithRetry {
            restClient.get()
                .uri("https://api.twitch.tv/helix/users")
                .header("Authorization", "Bearer $appOauthToken")
                .header("Client-Id", twitchProperties.appClientId)
                .retrieve()
                .body(TwitchUsersResponseDto::class.java)
        }
    }

    /**
     * 트위치 스트림 정보 불러오기
     * https://dev.twitch.tv/docs/api/reference/#get-streams
     */
    fun getStreams(request: TwitchStreamsRequestDto): TwitchStreamsResponseDto? {
        return executeWithRetry {
            restClient.get()
                .uri("https://api.twitch.tv/helix/streams")
                .header("Authorization", "Bearer $appOauthToken")
                .header("Client-Id", twitchProperties.appClientId)
                .retrieve()
                .body(TwitchStreamsResponseDto::class.java)
        }
    }

    /**
     * 401 에러 발생 시 OAuth 토큰 재발급 후 API 호출 재시도
     *
     * @param block API 호출 람다 함수
     * @return API 호출 결과
     */
    private fun <T> executeWithRetry(block: () -> T?): T? {
        return try {
            block()
        } catch (e: RestClientResponseException) {
            if (e.statusCode.value() == 401) {
                logger.warn("Twitch API 401 Unauthorized - refreshing OAuth token")
                refreshOauthToken()
                block()
            } else {
                logger.error("Twitch API call failed")
                logger.error("Status: ${e.statusCode.value()} ${e.statusText}")
                logger.error("Headers: ${e.responseHeaders}")
                logger.error("Response Body: ${e.responseBodyAsString}")
                throw e
            }
        }
    }

}