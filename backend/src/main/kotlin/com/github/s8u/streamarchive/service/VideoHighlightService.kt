package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.HighlightParameters
import com.github.s8u.streamarchive.dto.HighlightSegment
import com.github.s8u.streamarchive.dto.VideoHighlightsResponse
import com.github.s8u.streamarchive.entity.VideoDataChatHistory
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.VideoDataChatHistoryRepository
import com.github.s8u.streamarchive.repository.VideoRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.min

@Service
class VideoHighlightService(
    private val videoDataChatHistoryRepository: VideoDataChatHistoryRepository,
    private val videoRepository: VideoRepository,
    private val authenticationService: AuthenticationService
) {
    private val logger = LoggerFactory.getLogger(VideoHighlightService::class.java)

    @Transactional(readOnly = true)
    fun getHighlights(
        videoUuid: String,
        params: HighlightParameters = HighlightParameters()
    ): VideoHighlightsResponse {
        logger.debug("Calculating highlights for video: {}, params: {}", videoUuid, params)

        // Video 조회 및 Privacy 체크
        val video = videoRepository.findByUuid(videoUuid)
            ?: throw BusinessException("Video not found", HttpStatus.NOT_FOUND)

        if (video.contentPrivacy == ContentPrivacy.PRIVATE && !authenticationService.isAdmin()) {
            throw BusinessException("Video not found", HttpStatus.NOT_FOUND)
        }

        val totalDurationMs = video.duration * 1000L

        // 모든 채팅 히스토리 조회
        val chats = videoDataChatHistoryRepository
            .findByVideoIdAndOffsetMillisGreaterThanEqualAndOffsetMillisLessThanOrderByOffsetMillisAsc(
                videoId = video.id!!,
                startOffset = 0L,
                endOffset = totalDurationMs
            )

        logger.debug("Found {} chat messages for video {}", chats.size, videoUuid)

        if (chats.isEmpty()) {
            logger.debug("No chat messages found, returning empty highlights")
            return VideoHighlightsResponse(
                videoUuid = videoUuid,
                totalDurationMs = totalDurationMs,
                highlights = emptyList(),
                totalHighlightDurationMs = 0L,
                parameters = params
            )
        }

        // 채팅 밀도 계산
        val densityMap = calculateChatDensity(chats, totalDurationMs, params.windowSizeSeconds)

        // 임계값 계산
        val threshold = calculateThreshold(densityMap, params.thresholdPercentile)

        logger.debug("Calculated threshold: {} chats per window", threshold)

        // 하이라이트 구간 추출
        val rawSegments = extractSegments(densityMap, threshold, params.windowSizeSeconds)

        logger.debug("Extracted {} raw segments", rawSegments.size)

        // 인접 구간 병합
        val mergedSegments = mergeNearbySegments(rawSegments, params.mergeGapSeconds * 1000L)

        logger.debug("Merged to {} segments", mergedSegments.size)

        // 최소 길이 필터링
        val finalSegments = mergedSegments.filter {
            (it.endOffsetMs - it.startOffsetMs) >= params.minSegmentSeconds * 1000L
        }

        logger.debug("Final {} segments after filtering", finalSegments.size)

        val totalHighlightDurationMs = finalSegments.sumOf { it.endOffsetMs - it.startOffsetMs }

        return VideoHighlightsResponse(
            videoUuid = videoUuid,
            totalDurationMs = totalDurationMs,
            highlights = finalSegments,
            totalHighlightDurationMs = totalHighlightDurationMs,
            parameters = params
        )
    }

    private fun calculateChatDensity(
        chats: List<VideoDataChatHistory>,
        totalDurationMs: Long,
        windowSizeSeconds: Int
    ): Map<Long, Int> {
        val windowMs = windowSizeSeconds * 1000L
        val density = mutableMapOf<Long, Int>()

        // 슬라이딩 윈도우로 전체 구간 스캔 (50% 오버랩)
        var windowStart = 0L
        val step = windowMs / 2

        while (windowStart < totalDurationMs) {
            val windowEnd = min(windowStart + windowMs, totalDurationMs)

            // 해당 윈도우 내 채팅 수 계산
            val count = chats.count { chat ->
                chat.offsetMillis >= windowStart && chat.offsetMillis < windowEnd
            }

            density[windowStart] = count
            windowStart += step
        }

        return density
    }

    private fun calculateThreshold(
        densityMap: Map<Long, Int>,
        percentile: Int
    ): Int {
        if (densityMap.isEmpty()) return 0

        val sorted = densityMap.values.sorted()
        val index = (sorted.size * percentile / 100.0).toInt()
            .coerceIn(0, sorted.size - 1)
        return sorted[index]
    }

    private fun extractSegments(
        densityMap: Map<Long, Int>,
        threshold: Int,
        windowSizeSeconds: Int
    ): List<HighlightSegment> {
        if (threshold == 0) return emptyList()

        val segments = mutableListOf<HighlightSegment>()
        var currentSegment: HighlightSegment? = null

        val windowMs = windowSizeSeconds * 1000L

        densityMap.entries.sortedBy { it.key }.forEach { (offset, count) ->
            if (count >= threshold) {
                if (currentSegment == null) {
                    // 새 구간 시작
                    currentSegment = HighlightSegment(
                        startOffsetMs = offset,
                        endOffsetMs = offset + windowMs,
                        chatCount = count,
                        peakChatRate = count.toDouble() / windowSizeSeconds
                    )
                } else {
                    // 기존 구간 확장
                    currentSegment = currentSegment!!.copy(
                        endOffsetMs = offset + windowMs,
                        chatCount = currentSegment!!.chatCount + count,
                        peakChatRate = maxOf(
                            currentSegment!!.peakChatRate,
                            count.toDouble() / windowSizeSeconds
                        )
                    )
                }
            } else {
                // 구간 종료
                currentSegment?.let { segments.add(it) }
                currentSegment = null
            }
        }

        // 마지막 구간 추가
        currentSegment?.let { segments.add(it) }

        return segments
    }

    private fun mergeNearbySegments(
        segments: List<HighlightSegment>,
        gapMs: Long
    ): List<HighlightSegment> {
        if (segments.isEmpty()) return emptyList()

        val merged = mutableListOf<HighlightSegment>()
        var current = segments.first()

        segments.drop(1).forEach { segment ->
            if (segment.startOffsetMs - current.endOffsetMs <= gapMs) {
                // 병합
                current = HighlightSegment(
                    startOffsetMs = current.startOffsetMs,
                    endOffsetMs = segment.endOffsetMs,
                    chatCount = current.chatCount + segment.chatCount,
                    peakChatRate = maxOf(current.peakChatRate, segment.peakChatRate)
                )
            } else {
                merged.add(current)
                current = segment
            }
        }

        merged.add(current)
        return merged
    }
}
