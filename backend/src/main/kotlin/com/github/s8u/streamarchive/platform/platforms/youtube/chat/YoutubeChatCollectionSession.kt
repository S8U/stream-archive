package com.github.s8u.streamarchive.platform.platforms.youtube.chat

import com.github.s8u.streamarchive.platform.chat.PlatformChatCollectionSession
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatMessageDto
import com.github.s8u.streamarchive.platform.platforms.youtube.client.YoutubeApiClient
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeLiveChatMessage
import org.slf4j.LoggerFactory
import org.springframework.web.client.RestClientResponseException
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 유튜브 채팅 수집 세션
 */
class YoutubeChatCollectionSession(
    private val apiClient: YoutubeApiClient,
    private val recordId: Long,
    private val videoId: Long,
    private val liveChatId: String,
    private val recordStartedAt: LocalDateTime,
    private val onChat: (PlatformChatMessageDto) -> Unit,
    private val onClosed: () -> Unit
) : PlatformChatCollectionSession {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val isRunning = AtomicBoolean(false)
    private lateinit var thread: Thread

    /**
     * 유튜브 채팅 수집을 시작합니다.
     */
    fun start() {
        isRunning.set(true)
        thread = Thread {
            collect()
        }.apply {
            isDaemon = true
            name = "youtube-chat-$recordId"
            start()
        }
    }

    override fun stop() {
        isRunning.set(false)
        if (::thread.isInitialized) {
            thread.interrupt()
        }
    }

    private fun collect() {
        var pageToken: String? = null

        try {
            while (isRunning.get()) {
                // 응답을 못 받으면 일시적 끊김으로 보고 재연결을 맡긴다
                val response = apiClient.getLiveChatMessages(
                    liveChatId = liveChatId,
                    pageToken = pageToken
                )
                if (response == null) {
                    notifyClosed()
                    return
                }

                response.items
                    .mapNotNull { toChatMessage(it) }
                    .forEach(onChat)

                // 유튜브가 채팅 종료를 알리면 방송이 끝난 것이므로 재연결하지 않는다
                if (response.offlineAt != null) {
                    logger.info("YoutubeChatCollectionSession: YouTube chat ended: recordId={}", recordId)
                    return
                }

                // 다음 페이지 토큰이 없으면 일시적 끊김으로 보고 재연결을 맡긴다
                pageToken = response.nextPageToken
                if (pageToken == null) {
                    notifyClosed()
                    return
                }

                Thread.sleep(response.pollingIntervalMillis ?: DEFAULT_POLLING_INTERVAL_MILLIS)
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (e: RestClientResponseException) {
            // 채팅을 더 못 받는 상태(권한 없음/없음)면 방송이 끝난 것으로 본다
            if (e.statusCode.value() == 403 || e.statusCode.value() == 404) {
                logger.warn(
                    "YoutubeChatCollectionSession: YouTube chat collect ended: recordId={}, status={}",
                    recordId,
                    e.statusCode.value()
                )
                return
            }

            logger.error("YoutubeChatCollectionSession: YouTube chat collect failed: recordId={}", recordId, e)
            notifyClosed()
        } catch (e: Exception) {
            logger.error("YoutubeChatCollectionSession: YouTube chat collect failed: recordId={}", recordId, e)
            notifyClosed()
        }
    }

    private fun toChatMessage(message: YoutubeLiveChatMessage): PlatformChatMessageDto? {
        val text = message.snippet.textMessageDetails?.messageText
            ?: message.snippet.displayMessage
            ?: return null
        val publishedAt = message.snippet.publishedAt ?: return null
        val time = Instant.parse(publishedAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val offsetMillis = Duration.between(recordStartedAt, time).toMillis()

        // 첫 요청에 포함된 과거 채팅은 현재 녹화 채팅에서 제외한다
        if (offsetMillis < 0) {
            return null
        }

        return PlatformChatMessageDto(
            recordId = recordId,
            videoId = videoId,
            username = message.authorDetails?.displayName ?: "unknown",
            message = text,
            offsetMillis = offsetMillis,
            createdAt = time
        )
    }

    // 끊김을 호출자에게 알린다 (재연결 여부는 호출자가 정한다)
    private fun notifyClosed() {
        if (isRunning.get()) {
            onClosed()
        }
    }

    companion object {
        private const val DEFAULT_POLLING_INTERVAL_MILLIS = 5000L
    }

}
