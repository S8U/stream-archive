package com.github.s8u.streamarchive.platform.platforms.youtube.client

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeChannelsResponse
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatMessagesFetchResult
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatMessagesResponse
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubePlaylistItemsResponse
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeVideosResponse
import com.github.s8u.streamarchive.platform.platforms.youtube.properties.YoutubeProperties
import org.slf4j.LoggerFactory
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

/**
 * 유튜브 API 클라이언트
 */
@Component
class YoutubeApiClient(
    private val youtubeProperties: YoutubeProperties,
    private val platformApiClientRequestFactory: ClientHttpRequestFactory
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val objectMapper = ObjectMapper().apply {
        registerKotlinModule()
        propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private val restClient: RestClient = RestClient.builder()
        .requestFactory(platformApiClientRequestFactory)
        .messageConverters { converters ->
            converters.removeIf { it is MappingJackson2HttpMessageConverter }
            converters.add(MappingJackson2HttpMessageConverter(objectMapper))
        }
        .build()

    /**
     * 유튜브 채널 정보를 조회합니다.
     * https://developers.google.com/youtube/v3/docs/channels/list
     */
    fun getChannel(channelId: String): YoutubeChannelsResponse? {
        return execute(
            operation = "getChannel",
            context = "channelId=$channelId"
        ) {
            restClient.get()
                .uri("https://www.googleapis.com/youtube/v3/channels") { uriBuilder ->
                    uriBuilder
                        .queryParam("part", "snippet")
                        .queryParam("id", channelId)
                        .queryParam("key", youtubeProperties.apiKey)
                        .build()
                }
                .retrieve()
                .body(YoutubeChannelsResponse::class.java)
        }
    }

    /**
     * 유튜브 핸들로 채널 정보를 조회합니다.
     * https://developers.google.com/youtube/v3/docs/channels/list
     */
    fun getChannelByHandle(handle: String): YoutubeChannelsResponse? {
        return execute(
            operation = "getChannelByHandle",
            context = "handle=$handle"
        ) {
            restClient.get()
                .uri("https://www.googleapis.com/youtube/v3/channels") { uriBuilder ->
                    uriBuilder
                        .queryParam("part", "snippet")
                        .queryParam("forHandle", handle)
                        .queryParam("key", youtubeProperties.apiKey)
                        .build()
                }
                .retrieve()
                .body(YoutubeChannelsResponse::class.java)
        }
    }

    /**
     * 재생목록의 동영상 항목을 조회합니다.
     * https://developers.google.com/youtube/v3/docs/playlistItems/list
     */
    fun getPlaylistItems(
        playlistId: String,
        maxResults: Int
    ): YoutubePlaylistItemsResponse? {
        return execute(
            operation = "getPlaylistItems",
            context = "playlistId=$playlistId"
        ) {
            restClient.get()
                .uri("https://www.googleapis.com/youtube/v3/playlistItems") { uriBuilder ->
                    uriBuilder
                        .queryParam("part", "contentDetails")
                        .queryParam("playlistId", playlistId)
                        .queryParam("maxResults", maxResults)
                        .queryParam("key", youtubeProperties.apiKey)
                        .build()
                }
                .retrieve()
                .body(YoutubePlaylistItemsResponse::class.java)
        }
    }

    /**
     * 유튜브 동영상 정보를 조회합니다.
     * https://developers.google.com/youtube/v3/docs/videos/list
     */
    fun getVideos(videoIds: List<String>): YoutubeVideosResponse? {
        // 동영상 ID를 한 번에 묶어 호출당 쿼터(1 unit)로 조회한다
        if (videoIds.isEmpty()) {
            return YoutubeVideosResponse()
        }

        return execute(
            operation = "getVideos",
            context = "videoIds=${videoIds.joinToString(",")}"
        ) {
            restClient.get()
                .uri("https://www.googleapis.com/youtube/v3/videos") { uriBuilder ->
                    uriBuilder
                        .queryParam("part", "snippet,liveStreamingDetails")
                        .queryParam("id", videoIds.joinToString(","))
                        .queryParam("key", youtubeProperties.apiKey)
                        .build()
                }
                .retrieve()
                .body(YoutubeVideosResponse::class.java)
        }
    }

    /**
     * 유튜브 라이브 채팅 메시지를 조회합니다.
     * https://developers.google.com/youtube/v3/live/docs/liveChatMessages/list
     */
    fun getLiveChatMessages(
        liveChatId: String,
        pageToken: String?
    ): YoutubeLiveChatMessagesFetchResult? {
        return execute(
            operation = "getLiveChatMessages",
            context = "liveChatId=$liveChatId"
        ) {
            val rawBody = restClient.get()
                .uri("https://www.googleapis.com/youtube/v3/liveChat/messages") { uriBuilder ->
                    uriBuilder
                        .queryParam("part", "snippet,authorDetails")
                        .queryParam("liveChatId", liveChatId)
                        .queryParam("maxResults", 200)
                        .queryParam("key", youtubeProperties.apiKey)
                    pageToken?.let { uriBuilder.queryParam("pageToken", it) }
                    uriBuilder.build()
                }
                .retrieve()
                .body(String::class.java)
                ?: return@execute null

            // 응답 형식이 바뀌어 파싱이 깨져도 원본은 남길 수 있게 본문을 함께 돌려준다
            val response = try {
                objectMapper.readValue(rawBody, YoutubeLiveChatMessagesResponse::class.java)
            } catch (e: JsonProcessingException) {
                logger.warn("YoutubeApiClient: Failed to parse live chat messages: liveChatId={}", liveChatId, e)
                null
            }

            YoutubeLiveChatMessagesFetchResult(
                response = response,
                rawBody = rawBody
            )
        }
    }

    private fun <T> execute(
        operation: String,
        context: String,
        block: () -> T?
    ): T? {
        if (youtubeProperties.apiKey.isBlank()) {
            logger.warn("YoutubeApiClient: YouTube API key is empty: operation={}, context={}", operation, context)
            return null
        }

        return try {
            block()
        } catch (e: RestClientResponseException) {
            logger.error(
                "YoutubeApiClient: YouTube API call failed: operation={}, context={}, status={}, statusText={}",
                operation,
                context,
                e.statusCode.value(),
                e.statusText
            )

            throw e
        }
    }

}
