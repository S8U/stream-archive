package com.github.s8u.streamarchive.client.chzzk

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

    private val logger = LoggerFactory.getLogger(ChzzkApiClient::class.java)

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
                .retrieve()
                .body(object : ParameterizedTypeReference<ChzzkResponseDto<ChzzkLiveDetailDto>>() {})
        }
    }

    private fun <T> execute(block: () -> T?): T? {
        return try {
            block()
        } catch (e: RestClientResponseException) {
            logger.error("Chzzk API call failed")
            logger.error("Status: ${e.statusCode.value()} ${e.statusText}")
            logger.error("Headers: ${e.responseHeaders}")
            logger.error("Response Body: ${e.responseBodyAsString}")

            throw e
        }
    }

}