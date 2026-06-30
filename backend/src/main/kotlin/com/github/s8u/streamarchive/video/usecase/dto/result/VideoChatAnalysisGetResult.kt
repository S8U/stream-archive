package com.github.s8u.streamarchive.video.usecase.dto.result

import com.github.s8u.streamarchive.video.service.dto.VideoChatAnalyzeResult

/**
 * 동영상 채팅 분석 조회 결과
 *
 * 구간(버킷)별 채팅 개수와, 채팅이 몰린 구간의 대표 키워드를 담는다.
 */
data class VideoChatAnalysisGetResult(
    val bucketMillis: Long,
    val buckets: List<Bucket>
) {

    /**
     * 한 구간의 채팅 분석 결과.
     *
     * @param offsetMillis 구간 시작 오프셋 (밀리초)
     * @param count 구간 채팅 개수
     * @param keywords 구간 대표 키워드 (피크 구간에만 채워지고, 그 외에는 비어 있다)
     */
    data class Bucket(
        val offsetMillis: Long,
        val count: Long,
        val keywords: List<Keyword>
    )

    /**
     * 구간 대표 키워드.
     *
     * @param keyword 형태소 분석으로 뽑은 키워드
     * @param count 구간 내 등장 횟수
     * @param label 그 키워드를 포함한 실제 채팅 원문 중 가장 흔한 것
     */
    data class Keyword(
        val keyword: String,
        val count: Int,
        val label: String
    )

    companion object {
        fun from(analyzeResult: VideoChatAnalyzeResult): VideoChatAnalysisGetResult {
            return VideoChatAnalysisGetResult(
                bucketMillis = analyzeResult.bucketMillis,
                buckets = analyzeResult.buckets.map { bucket ->
                    Bucket(
                        offsetMillis = bucket.offsetMillis,
                        count = bucket.count,
                        keywords = bucket.keywords.map { keyword ->
                            Keyword(
                                keyword = keyword.keyword,
                                count = keyword.count,
                                label = keyword.label
                            )
                        }
                    )
                }
            )
        }
    }

}
