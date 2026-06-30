package com.github.s8u.streamarchive.video.service

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class VideoChatKeywordExtractServiceTest {

    private val videoChatKeywordExtractService = VideoChatKeywordExtractService()

    @Nested
    inner class Extract {

        @Test
        fun `명사를 키워드로 뽑는다`() {
            val keywords = videoChatKeywordExtractService.extract("대머리 컷ㅠㅠ")

            assertTrue("대머리" in keywords, "추출 결과: $keywords")
        }

        @Test
        fun `한 글자 토큰은 키워드에서 제외한다`() {
            val keywords = videoChatKeywordExtractService.extract("개 잘하네")

            assertTrue(keywords.none { it.length < 2 }, "추출 결과: $keywords")
        }

        @Test
        fun `조사와 어미는 키워드에서 제외한다`() {
            val keywords = videoChatKeywordExtractService.extract("번개가 떨어졌다")

            assertTrue("가" !in keywords && "다" !in keywords, "추출 결과: $keywords")
        }

        @Test
        fun `빈 메시지는 빈 목록을 반환한다`() {
            assertTrue(videoChatKeywordExtractService.extract("   ").isEmpty())
        }

        @Test
        fun `플랫폼이 분리해준 이모티콘 코드는 키워드에서 제외한다`() {
            val placeholders = listOf("{:soop-ogq-65170e582b133-5:}")
            val keywords = videoChatKeywordExtractService.extract("{:soop-ogq-65170e582b133-5:} 우승", placeholders)

            assertTrue("soop" !in keywords && "ogq" !in keywords, "추출 결과: $keywords")
            assertTrue("우승" in keywords, "추출 결과: $keywords")
        }

        @Test
        fun `이모티콘만 있는 메시지는 빈 목록을 반환한다`() {
            val placeholder = "{:soop-ogq-65170e582b133-5:}"
            assertTrue(videoChatKeywordExtractService.extract(placeholder, listOf(placeholder)).isEmpty())
        }

        @Test
        fun `같은 메시지 안의 중복 키워드는 한 번만 담는다`() {
            val keywords = videoChatKeywordExtractService.extract("머리 머리 머리")

            assertTrue(keywords.count { it == "머리" } <= 1, "추출 결과: $keywords")
        }
    }

    @Nested
    inner class RemoveEmoticons {

        @Test
        fun `트위치식 평문 단어 이모티콘도 목록으로 넘기면 제거한다`() {
            // 트위치는 Kappa 같은 평문 단어가 이모티콘 코드다.
            // 정규식으로는 못 거르지만 목록으로 주면 제거된다.
            val result = videoChatKeywordExtractService.removeEmoticons("개웃기네 Kappa", listOf("Kappa"))

            assertTrue(result == "개웃기네", "정제 결과: '$result'")
        }

        @Test
        fun `이모티콘 코드를 걷어내고 공백을 정리한다`() {
            val result = videoChatKeywordExtractService.removeEmoticons(
                "{:soop-ogq-1:} 우승 {:soop-ogq-2:}",
                listOf("{:soop-ogq-1:}", "{:soop-ogq-2:}")
            )

            assertTrue(result == "우승", "정제 결과: '$result'")
        }
    }

}
