package com.github.s8u.streamarchive.client.soop

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class SoopApiClient {

    private val logger = LoggerFactory.getLogger(SoopApiClient::class.java)

    private val restClient: RestClient = RestClient.builder()
        .messageConverters { converters ->
            val objectMapper = ObjectMapper().apply {
                registerKotlinModule()
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
            converters.removeIf { it is MappingJackson2HttpMessageConverter }

            // text/html도 JSON으로 파싱
            val jsonConverter = MappingJackson2HttpMessageConverter(objectMapper).apply {
                supportedMediaTypes = listOf(
                    MediaType.APPLICATION_JSON,
                    MediaType("text", "html")
                )
            }
            converters.add(jsonConverter)
        }
        .build()

    /**
     * 채널 정보 조회
     */
    fun getStation(userId: String): SoopStationResponseDto? {
        return try {
            restClient.get()
                .uri("https://chapi.sooplive.co.kr/api/$userId/station")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .retrieve()
                .body(SoopStationResponseDto::class.java)
        } catch (e: RestClientResponseException) {
            if (e.statusCode.value() == 515) {
                return null
            }
            
            throw e
        }
    }

    /**
     * 라이브 상세 정보 조회
     * Content-Type이 text/html이지만 JSON 반환함
     */
    fun getLiveDetail(userId: String): SoopLiveResponseDto? {
        return try {
            val formData = LinkedMultiValueMap<String, String>().apply {
                add("bid", userId)
                add("type", "live")
                add("pwd", "")
                add("player_type", "html5")
                add("stream_type", "common")
                add("quality", "HD")
                add("mode", "landing")
                add("from_api", "0")
                add("is_revive", "false")
            }

            restClient.post()
                .uri("https://live.sooplive.co.kr/afreeca/player_live_api.php?bjid=$userId")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(SoopLiveResponseDto::class.java)
        } catch (e: RestClientResponseException) {
            logger.error("SOOP Live API call failed")
            logger.error("Status: ${e.statusCode.value()} ${e.statusText}")
            logger.error("Headers: ${e.responseHeaders}")
            logger.error("Response Body: ${e.responseBodyAsString}")
            throw e
        } catch (e: Exception) {
            logger.error("SOOP Live API call failed with exception", e)
            throw e
        }
    }

}
