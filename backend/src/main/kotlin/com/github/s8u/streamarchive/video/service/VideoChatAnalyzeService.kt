package com.github.s8u.streamarchive.video.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.s8u.streamarchive.platform.chat.PlatformEmoticonStripper
import com.github.s8u.streamarchive.platform.chat.PlatformEmoticonStripperFactory
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.video.repository.dto.VideoChatMessageProjection
import com.github.s8u.streamarchive.video.service.dto.VideoChatAnalyzeResult
import org.springframework.stereotype.Service

/**
 * 동영상 채팅을 구간별로 분석한다.
 *
 * 전체 채팅을 구간(버킷)으로 묶어 개수를 센다.
 * 구간별로 형태소 키워드 빈도와 대표 채팅 원문을 뽑는다.
 * 외부 조회 없이 받은 데이터로만 계산하는 순수 도메인 서비스다.
 */
@Service
class VideoChatAnalyzeService(
    private val videoChatKeywordExtractService: VideoChatKeywordExtractService,
    private val platformEmoticonStripperFactory: PlatformEmoticonStripperFactory,
    private val objectMapper: ObjectMapper
) {

    /**
     * @param messages 동영상 전체 채팅(오프셋·원문), 오프셋 오름차순
     * @param bucketMillis 구간 길이 (밀리초)
     * @param keywordCount 구간별 대표 키워드 개수
     * @param platformType 채팅을 수집한 플랫폼 (emojis로 분리되지 못한 이모티콘 코드를 그 플랫폼 형식으로 추가 제거한다)
     */
    fun analyze(
        messages: List<VideoChatMessageProjection>,
        bucketMillis: Long,
        keywordCount: Int,
        platformType: PlatformType? = null
    ): VideoChatAnalyzeResult {
        if (messages.isEmpty()) {
            return VideoChatAnalyzeResult(bucketMillis = bucketMillis, buckets = emptyList())
        }

        val emoticonStripper = platformType
            ?.let { platformEmoticonStripperFactory.findPlatformEmoticonStripper(it) }

        // 버킷 인덱스 → 그 구간 채팅 원문들
        val messagesByBucket = messages.groupBy { it.offsetMillis / bucketMillis }

        // 채팅이 없는 구간도 시간축이 끊기지 않게 0부터 마지막까지 모든 버킷을 채운다.
        val lastBucketIndex = messages.last().offsetMillis / bucketMillis

        val buckets = (0..lastBucketIndex).map { bucketIndex ->
            val bucketMessages = messagesByBucket[bucketIndex].orEmpty()
            // 채팅이 너무 적은 조용한 구간은 빈도가 무의미해 라벨이 들쭉날쭉하다.
            // 그래프만 두고 라벨은 비운다.
            val keywords = if (bucketMessages.size >= MIN_COUNT_FOR_KEYWORDS) {
                extractKeywords(bucketMessages, keywordCount, emoticonStripper)
            } else {
                emptyList()
            }
            VideoChatAnalyzeResult.Bucket(
                offsetMillis = bucketIndex * bucketMillis,
                count = bucketMessages.size.toLong(),
                keywords = keywords
            )
        }

        return VideoChatAnalyzeResult(bucketMillis = bucketMillis, buckets = buckets)
    }

    /**
     * 한 구간의 채팅들에서 키워드 빈도를 집계하고, 키워드별 대표 원문을 고른다.
     */
    private fun extractKeywords(
        messages: List<VideoChatMessageProjection>,
        keywordCount: Int,
        emoticonStripper: PlatformEmoticonStripper?
    ): List<VideoChatAnalyzeResult.Keyword> {
        if (messages.isEmpty()) return emptyList()

        // 키워드 → 등장 횟수
        val keywordFrequency = HashMap<String, Int>()
        // 키워드 → (원문 → 그 원문 등장 횟수): 키워드별 대표 원문 선정용
        val keywordMessageFrequency = HashMap<String, HashMap<String, Int>>()

        for (chat in messages) {
            // emojis로 분리되지 못한 이모티콘 코드는 플랫폼 형식으로 먼저 걷어낸다.
            val message = emoticonStripper?.strip(chat.message) ?: chat.message
            // 플랫폼이 분리해둔 실제 이모티콘 코드도 제거한다(형식 추측 없음).
            val placeholders = parsePlaceholders(chat.emojis)
            val keywords = videoChatKeywordExtractService.extract(message, placeholders)
            // 라벨은 이모티콘 코드를 걷어낸 원문으로 집계한다(키워드 빈도와는 별개).
            val label = videoChatKeywordExtractService.removeEmoticons(message, placeholders)
            if (label.isBlank()) continue

            for (keyword in keywords) {
                keywordFrequency.merge(keyword, 1, Int::plus)
                keywordMessageFrequency
                    .getOrPut(keyword) { HashMap() }
                    .merge(label, 1, Int::plus)
            }
        }

        return keywordFrequency.entries
            // 그 구간에서 1번만 나온 키워드는 대표성이 약하다(들쭉날쭉한 라벨의 원인). 거른다.
            .filter { it.value >= MIN_KEYWORD_FREQUENCY }
            .sortedByDescending { it.value }
            .take(keywordCount)
            .map { (keyword, count) ->
                val label = keywordMessageFrequency[keyword]
                    ?.maxByOrNull { it.value }
                    ?.key
                    ?.let { truncateLabel(it) }
                    ?: keyword

                VideoChatAnalyzeResult.Keyword(
                    keyword = keyword,
                    count = count,
                    label = label
                )
            }
    }

    /** 이모티콘 매핑 JSON(`{placeholder: filename}`)에서 placeholder(키) 목록만 뽑는다. */
    private fun parsePlaceholders(emojisJson: String?): Collection<String> {
        if (emojisJson.isNullOrBlank()) return emptyList()
        return try {
            objectMapper.readValue(emojisJson, object : TypeReference<Map<String, String>>() {}).keys
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** 도배성 긴 복붙 채팅은 라벨로 길어서 보기 나쁘다. 적당히 잘라낸다. */
    private fun truncateLabel(label: String): String {
        return if (label.length <= MAX_LABEL_LENGTH) label else label.take(MAX_LABEL_LENGTH).trimEnd() + "…"
    }

    companion object {
        private const val MAX_LABEL_LENGTH = 30

        /** 구간 채팅이 이 수 미만이면 빈도가 무의미해 라벨을 붙이지 않는다. */
        private const val MIN_COUNT_FOR_KEYWORDS = 5

        /** 구간에서 이 횟수 미만으로 나온 키워드는 대표성이 약해 라벨로 쓰지 않는다. */
        private const val MIN_KEYWORD_FREQUENCY = 2
    }

}
