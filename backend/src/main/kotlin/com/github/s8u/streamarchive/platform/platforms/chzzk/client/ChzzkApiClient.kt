package com.github.s8u.streamarchive.platform.platforms.chzzk.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class ChzzkApiClient {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val restClient: RestClient = RestClient.builder()
        .messageConverters { converters ->
            val objectMapper = ObjectMapper().apply {
                registerKotlinModule()
                propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
            converters.removeIf { it is MappingJackson2HttpMessageConverter }
            converters.add(MappingJackson2HttpMessageConverter(objectMapper))
        }
        .build()

    /**
     * 채널 정보 조회
     */
    fun getChannel(channelId: String): ChzzkResponseDto<ChzzkChannelDto>? {
        return execute {
            restClient.get()
                .uri("https://api.chzzk.naver.com/service/v1/channels/$channelId")
                .chzzkHeaders()
                .retrieve()
                .body(object : ParameterizedTypeReference<ChzzkResponseDto<ChzzkChannelDto>>() {})
        }
    }

    /**
     * 라이브 상세 정보 조회
     */
    fun getLiveDetail(channelId: String): ChzzkResponseDto<ChzzkLiveDetailDto>? {
        return execute {
            restClient.get()
                .uri("https://api.chzzk.naver.com/service/v2/channels/$channelId/live-detail")
                .chzzkHeaders()
                .retrieve()
                .body(object : ParameterizedTypeReference<ChzzkResponseDto<ChzzkLiveDetailDto>>() {})
        }
    }

    /**
     * 채팅 액세스 토큰 조회
     */
    fun getChatAccessToken(chatChannelId: String): ChzzkResponseDto<ChzzkChatAccessTokenDto>? {
        return execute {
            restClient.get()
                .uri("https://comm-api.game.naver.com/nng_main/v1/chats/access-token?channelId=$chatChannelId&chatType=STREAMING")
                .chzzkHeaders()
                .retrieve()
                .body(object : ParameterizedTypeReference<ChzzkResponseDto<ChzzkChatAccessTokenDto>>() {})
        }
    }

    private fun RestClient.RequestHeadersSpec<*>.chzzkHeaders(): RestClient.RequestHeadersSpec<*> {
        return this
            .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Accept", "application/json, text/plain, */*")
            .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
            .header("Origin", "https://chzzk.naver.com")
            .header("Referer", "https://chzzk.naver.com/")
    }

    private fun <T> execute(block: () -> T?): T? {
        return try {
            block()
        } catch (e: RestClientResponseException) {
            logger.error("ChzzkApiClient: Chzzk API call failed")
            logger.error("Status: ${e.statusCode.value()} ${e.statusText}")
            logger.error("Headers: ${e.responseHeaders}")
            logger.error("Response Body: ${e.responseBodyAsString}")

            throw e
        }
    }

}
