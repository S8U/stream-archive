package com.github.s8u.streamarchive.platform.platforms.soop.service

import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatEmojiDto
import com.github.s8u.streamarchive.platform.platforms.soop.client.SoopApiClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

/**
 * SOOP 채팅 이모티콘 해석
 *
 * 채팅 메시지 안의 `/키워드/` 토큰을 공통 매니페스트의 기본·구독 이모티콘 이미지로 바꾼다.
 * 스트리머 이모티콘처럼 OGQ 패킷으로 들어오는 이모티콘은 WebSocket 핸들러에서 따로 처리한다.
 * 매니페스트를 일정 시간 캐싱하고, Strategy와 핸들러가 같은 매핑을 쓰도록 한 곳에 모은다.
 */
@Service
class SoopChatEmoticonResolveService(
    private val apiClient: SoopApiClient,
    private val clock: Clock = Clock.systemUTC()
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // 키워드(/이름/) -> 이미지 URL
    private val cachedEmoticons = AtomicReference<SoopChatEmoticonCache>()

    /**
     * 메시지에서 SOOP 이모티콘을 찾아 공통 이모지 DTO로 바꾼다.
     *
     * 매칭되는 이모티콘이 없으면 빈 리스트를 반환한다.
     */
    fun resolve(message: String): List<PlatformChatEmojiDto> {
        if (!message.contains("/")) {
            return emptyList()
        }

        val emoticons = getEmoticons()
        if (emoticons.isEmpty()) {
            return emptyList()
        }

        return EMOTICON_REGEX
            .findAll(message)
            .map { it.value }
            .distinct()
            .mapNotNull { keyword ->
                val imageUrl = emoticons[keyword] ?: return@mapNotNull null
                PlatformChatEmojiDto(placeholder = keyword, imageUrl = imageUrl)
            }
            .toList()
    }

    private fun getEmoticons(): Map<String, String> {
        val now = Instant.now(clock)
        val cached = cachedEmoticons.get()
        if (cached != null && cached.expiresAt.isAfter(now)) {
            return cached.emoticons
        }

        val loaded = loadEmoticons()
        // 빈 결과는 호출 실패일 수 있어 캐싱하지 않고 다음에 다시 시도한다
        if (loaded.isNotEmpty()) {
            cachedEmoticons.set(
                SoopChatEmoticonCache(
                    emoticons = loaded,
                    expiresAt = now.plus(CACHE_TTL)
                )
            )
        }

        return loaded
    }

    private fun loadEmoticons(): Map<String, String> {
        return try {
            val manifest = apiClient.getChatEmoticonManifest() ?: return emptyMap()

            // 구독·기본 이모티콘을 합쳐 키워드별 이미지 URL로 만든다
            (manifest.subscribeEmoticons + manifest.defaultEmoticons)
                .filter { !it.isDeprecated }
                .mapNotNull { emoticon ->
                    val fileName = emoticon.fileName ?: return@mapNotNull null
                    emoticon.keyword to "$EMOTICON_IMAGE_BASE_URL$fileName"
                }
                .toMap()
        } catch (e: Exception) {
            logger.warn("SoopChatEmoticonResolveService: Failed to load SOOP chat emoticons", e)
            emptyMap()
        }
    }

    companion object {
        private const val EMOTICON_IMAGE_BASE_URL = "https://res.sooplive.com/images/chat/emoticon/big/"
        private val CACHE_TTL: Duration = Duration.ofHours(1)
        private val EMOTICON_REGEX = Regex("""/[^/\s]+/""")
    }

}
