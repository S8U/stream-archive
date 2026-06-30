package com.github.s8u.streamarchive.video.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.s8u.streamarchive.platform.chat.PlatformEmoticonStripperFactory
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.platform.platforms.chzzk.chat.ChzzkEmoticonStripper
import com.github.s8u.streamarchive.platform.platforms.soop.chat.SoopEmoticonStripper
import com.github.s8u.streamarchive.video.repository.dto.VideoChatMessageProjection
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class VideoChatAnalyzeServiceTest {

    private val videoChatAnalyzeService = VideoChatAnalyzeService(
        VideoChatKeywordExtractService(),
        PlatformEmoticonStripperFactory(listOf(ChzzkEmoticonStripper(), SoopEmoticonStripper())),
        jacksonObjectMapper()
    )

    private fun msg(offsetMillis: Long, message: String, emojis: String? = null) =
        VideoChatMessageProjection(offsetMillis, message, emojis)

    /** 한 구간에 같은 메시지를 여러 개 채운다(라벨이 붙는 최소 채팅 수를 넘기기 위함). */
    private fun fill(offsetMillis: Long, message: String, emojis: String? = null, n: Int = 5) =
        (0 until n).map { msg(offsetMillis + it, message, emojis) }

    @Test
    fun `버킷 인덱스를 오프셋으로 환산한다`() {
        val messages = fill(0, "안녕") + fill(125_000, "우승") // 0번, 2번 버킷

        val result = videoChatAnalyzeService.analyze(
            messages = messages,
            bucketMillis = 60_000,
            keywordCount = 2
        )

        assertEquals(0, result.buckets[0].offsetMillis)
        assertEquals(60_000, result.buckets[1].offsetMillis)
        assertEquals(120_000, result.buckets[2].offsetMillis)
    }

    @Test
    fun `채팅이 없는 중간 구간도 0으로 채운다`() {
        val messages = fill(0, "시작") + fill(125_000, "끝") // 1번 버킷은 비어 있음

        val result = videoChatAnalyzeService.analyze(
            messages = messages,
            bucketMillis = 60_000,
            keywordCount = 2
        )

        val emptyBucket = result.buckets.first { it.offsetMillis == 60_000L }
        assertEquals(0, emptyBucket.count)
        assertTrue(emptyBucket.keywords.isEmpty())
    }

    @Test
    fun `채팅이 충분한 구간에는 키워드가 채워진다`() {
        val messages = fill(0, "우승 가자") + fill(125_000, "역전 났다")

        val result = videoChatAnalyzeService.analyze(
            messages = messages,
            bucketMillis = 60_000,
            keywordCount = 2
        )

        assertTrue(result.buckets[0].keywords.isNotEmpty(), "0분 키워드: ${result.buckets[0].keywords}")
        assertTrue(result.buckets[2].keywords.isNotEmpty(), "2분 키워드: ${result.buckets[2].keywords}")
    }

    @Test
    fun `채팅이 적은 구간은 라벨을 붙이지 않는다`() {
        // 한 구간에 채팅 3개뿐이면 임계값(5) 미만이라 키워드가 비어야 한다.
        val messages = listOf(
            msg(0, "우승 가자"),
            msg(1000, "우승"),
            msg(2000, "우승했네")
        )

        val result = videoChatAnalyzeService.analyze(
            messages = messages,
            bucketMillis = 60_000,
            keywordCount = 2
        )

        assertEquals(3, result.buckets.first().count)
        assertTrue(result.buckets.first().keywords.isEmpty(), "키워드: ${result.buckets.first().keywords}")
    }

    @Test
    fun `키워드 대표 라벨로 가장 흔한 원문을 고른다`() {
        val messages = listOf(
            msg(0, "대머리 컷ㅠㅠ"),
            msg(1000, "대머리 컷ㅠㅠ"),
            msg(2000, "대머리 됐네"),
            msg(3000, "머리카락"),
            msg(4000, "안경")
        )

        val result = videoChatAnalyzeService.analyze(
            messages = messages,
            bucketMillis = 60_000,
            keywordCount = 3
        )

        val bucket = result.buckets.first()
        val daedaeri = bucket.keywords.firstOrNull { it.keyword == "대머리" }
        assertNotNull(daedaeri, "키워드 목록: ${bucket.keywords}")
        // '대머리'를 포함한 원문 중 가장 흔한 "대머리 컷ㅠㅠ"가 라벨이어야 한다.
        assertEquals("대머리 컷ㅠㅠ", daedaeri.label)
        assertEquals(3, daedaeri.count) // 대머리 컷ㅠㅠ x2 + 대머리 됐네 x1
    }

    @Test
    fun `이모티콘 코드는 라벨에서 걷어낸다`() {
        // emojis 컬럼에 저장된 실제 placeholder로 제거한다(플랫폼이 분리해둔 정보).
        val messages = fill(0, "{:soop-ogq-1:} 우승", emojis = """{"{:soop-ogq-1:}":"a.png"}""")

        val result = videoChatAnalyzeService.analyze(
            messages = messages,
            bucketMillis = 60_000,
            keywordCount = 1
        )

        val label = result.buckets.first().keywords.first().label
        assertTrue("soop" !in label && "{:" !in label, "라벨: '$label'")
    }

    @Test
    fun `긴 도배 라벨은 잘라낸다`() {
        val longMessage = "공약".repeat(50) // 100자
        val messages = fill(0, longMessage)

        val result = videoChatAnalyzeService.analyze(
            messages = messages,
            bucketMillis = 60_000,
            keywordCount = 1
        )

        val label = result.buckets.first().keywords.firstOrNull()?.label
        assertNotNull(label, "키워드가 없음")
        assertTrue(label.length <= 31, "라벨 길이: ${label.length}, 라벨: '$label'") // 30자 + …
    }

    @Test
    fun `emojis로 분리 안 된 이모티콘은 플랫폼 형식으로 걷어낸다`() {
        // 치지직 LCK 이모티콘처럼 emojis 컬럼이 비어도, platformType으로 {:lck_2:}를 제거한다.
        val messages = fill(0, "{:lck_2:}{:lck_2:} 한화 이겨라")

        val result = videoChatAnalyzeService.analyze(
            messages = messages,
            bucketMillis = 60_000,
            keywordCount = 1,
            platformType = PlatformType.CHZZK
        )

        val label = result.buckets.first().keywords.firstOrNull()?.label
        assertNotNull(label, "키워드 목록: ${result.buckets.first().keywords}")
        assertTrue("lck" !in label && "{:" !in label, "라벨: '$label'")
    }

}
