package com.github.s8u.streamarchive.video.controller.dto.response

import com.github.s8u.streamarchive.video.usecase.dto.result.VideoChatAnalysisGetResult
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "동영상 채팅 분석 응답")
data class VideoChatAnalysisGetResponse(
    @field:Schema(description = "구간 길이 (밀리초)", example = "60000")
    val bucketMillis: Long,

    @field:Schema(description = "구간별 채팅 분석 목록")
    val buckets: List<Bucket>
) {

    @Schema(description = "구간 채팅 분석")
    data class Bucket(
        @field:Schema(description = "구간 시작 오프셋 (밀리초)", example = "60000")
        val offsetMillis: Long,

        @field:Schema(description = "구간 채팅 개수", example = "152")
        val count: Long,

        @field:Schema(description = "구간 대표 키워드 (피크 구간에만 채워진다)")
        val keywords: List<Keyword>
    )

    @Schema(description = "구간 대표 키워드")
    data class Keyword(
        @field:Schema(description = "키워드", example = "대머리")
        val keyword: String,

        @field:Schema(description = "구간 내 등장 횟수", example = "18")
        val count: Int,

        @field:Schema(description = "키워드 대표 채팅 원문", example = "대머리 컷ㅠㅠ")
        val label: String
    )

    companion object {
        fun from(result: VideoChatAnalysisGetResult): VideoChatAnalysisGetResponse {
            return VideoChatAnalysisGetResponse(
                bucketMillis = result.bucketMillis,
                buckets = result.buckets.map { bucket ->
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
