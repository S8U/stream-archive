package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.HighlightResponse
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.ChatCountBucket
import com.github.s8u.streamarchive.repository.VideoDataChatHistoryRepository
import com.github.s8u.streamarchive.repository.VideoRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.sqrt

/**
 * 하이라이트 분석 서비스
 * 채팅 과열 구간을 분석하여 하이라이트 구간을 반환합니다.
 */
@Service
class HighlightService(
    private val videoDataChatHistoryRepository: VideoDataChatHistoryRepository,
    private val videoRepository: VideoRepository,
    private val authenticationService: AuthenticationService
) {
    companion object {
        const val BUCKET_SIZE_MILLIS = 10_000L       // 10초 버킷
        const val Z_SCORE_THRESHOLD = 1.5            // Z-Score 임계값
        const val MAX_HIGHLIGHTS = 10                // 최대 하이라이트 개수
        const val MIN_CHAT_COUNT = 5                 // 최소 채팅 수 (노이즈 필터링)
        const val MERGE_GAP_BUCKETS = 2              // 병합 허용 갭 (2버킷 = 20초)
    }

    /**
     * 하이라이트 클러스터 내부 클래스
     */
    private data class HighlightCluster(
        val startBucketIndex: Long,
        val endBucketIndex: Long,
        val buckets: List<BucketData>,
        val totalChatCount: Int,
        val peakBucketIndex: Long,
        val peakChatCount: Long,
        val maxZScore: Double
    ) {
        // 복합 점수 계산
        val score: Double
            get() {
                val durationWeight = 1.0 + (buckets.size - 1) * 0.2  // 연속 구간 가중치
                val peakBonus = if (isPeakCluster()) 1.5 else 1.0    // 피크 보너스
                return maxZScore * durationWeight * peakBonus
            }

        private fun isPeakCluster(): Boolean {
            // 클러스터 내에서 국소 최대값이 있는지 확인
            return buckets.size >= 2
        }
    }

    /**
     * 버킷 데이터 내부 클래스
     */
    private data class BucketData(
        val bucketIndex: Long,
        val chatCount: Long,
        val zScore: Double
    )

    /**
     * 동영상의 하이라이트 구간을 분석하여 반환합니다.
     */
    @Transactional(readOnly = true)
    fun getHighlights(uuid: String): List<HighlightResponse> {
        // 1. 비디오 조회 및 권한 체크
        val video = videoRepository.findByUuid(uuid)
            ?: throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)

        if (video.contentPrivacy == ContentPrivacy.PRIVATE && !authenticationService.isAdmin()) {
            throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        // 2. 버킷별 채팅 카운트 조회
        val rawBuckets = videoDataChatHistoryRepository.countChatsByBucket(
            videoId = video.id!!,
            bucketSizeMillis = BUCKET_SIZE_MILLIS
        )

        if (rawBuckets.isEmpty()) {
            return emptyList()
        }

        // 3. 통계 계산 (평균, 표준편차)
        val chatCounts = rawBuckets.map { it.getChatCount().toDouble() }
        val mean = chatCounts.average()
        val stdDev = calculateStandardDeviation(chatCounts, mean)

        // 표준편차가 0이면 의미있는 과열 구간이 없음
        if (stdDev == 0.0) {
            return emptyList()
        }

        // 4. Z-Score 계산 및 의미있는 버킷 필터링
        val bucketsWithZScore = rawBuckets.map { bucket ->
            val zScore = (bucket.getChatCount() - mean) / stdDev
            BucketData(
                bucketIndex = bucket.getBucketIndex(),
                chatCount = bucket.getChatCount(),
                zScore = zScore
            )
        }

        val significantBuckets = bucketsWithZScore.filter { 
            it.zScore >= Z_SCORE_THRESHOLD && it.chatCount >= MIN_CHAT_COUNT 
        }

        if (significantBuckets.isEmpty()) {
            return emptyList()
        }

        // 5. 피크 탐지 (Local Maxima)
        val peakBuckets = findPeaks(bucketsWithZScore, significantBuckets)

        if (peakBuckets.isEmpty()) {
            return emptyList()
        }

        // 6. 연속 구간 병합
        val clusters = mergeToClusters(peakBuckets, bucketsWithZScore)

        // 7. 점수 기준 정렬 및 상위 N개 선택
        val topClusters = clusters
            .sortedByDescending { it.score }
            .take(MAX_HIGHLIGHTS)
            .sortedBy { it.startBucketIndex }  // 시간순 정렬

        // 8. 응답 DTO로 변환
        val maxScore = topClusters.maxOfOrNull { it.score } ?: 1.0
        
        return topClusters.map { cluster ->
            HighlightResponse(
                startOffsetMillis = cluster.startBucketIndex * BUCKET_SIZE_MILLIS,
                endOffsetMillis = (cluster.endBucketIndex + 1) * BUCKET_SIZE_MILLIS,
                chatCount = cluster.totalChatCount,
                intensity = (cluster.score / maxScore).coerceIn(0.0, 1.0),  // 0~1 정규화
                peakOffsetMillis = cluster.peakBucketIndex * BUCKET_SIZE_MILLIS + (BUCKET_SIZE_MILLIS / 2)
            )
        }
    }

    /**
     * 표준편차 계산
     */
    private fun calculateStandardDeviation(values: List<Double>, mean: Double): Double {
        if (values.size < 2) return 0.0
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance)
    }

    /**
     * 피크 탐지 (Local Maxima)
     * 의미있는 버킷 중에서 양 옆보다 채팅 수가 많은 버킷만 선별
     */
    private fun findPeaks(
        allBuckets: List<BucketData>,
        significantBuckets: List<BucketData>
    ): List<BucketData> {
        val bucketMap = allBuckets.associateBy { it.bucketIndex }
        
        return significantBuckets.filter { bucket ->
            val prev = bucketMap[bucket.bucketIndex - 1]?.chatCount ?: 0
            val next = bucketMap[bucket.bucketIndex + 1]?.chatCount ?: 0
            bucket.chatCount > prev && bucket.chatCount >= next
        }
    }

    /**
     * 연속 구간 병합
     * 인접한 피크 버킷들을 하나의 클러스터로 병합
     */
    private fun mergeToClusters(
        peakBuckets: List<BucketData>,
        allBuckets: List<BucketData>
    ): List<HighlightCluster> {
        if (peakBuckets.isEmpty()) return emptyList()

        val sortedPeaks = peakBuckets.sortedBy { it.bucketIndex }
        val bucketMap = allBuckets.associateBy { it.bucketIndex }
        val clusters = mutableListOf<HighlightCluster>()

        var clusterStart = sortedPeaks.first().bucketIndex
        var clusterEnd = clusterStart
        var clusterBuckets = mutableListOf(sortedPeaks.first())

        for (i in 1 until sortedPeaks.size) {
            val current = sortedPeaks[i]
            val gap = current.bucketIndex - clusterEnd

            if (gap <= MERGE_GAP_BUCKETS + 1) {
                // 병합 허용 범위 내이면 클러스터 확장
                clusterEnd = current.bucketIndex
                clusterBuckets.add(current)
            } else {
                // 새 클러스터 시작
                clusters.add(createCluster(clusterStart, clusterEnd, clusterBuckets, bucketMap))
                clusterStart = current.bucketIndex
                clusterEnd = clusterStart
                clusterBuckets = mutableListOf(current)
            }
        }

        // 마지막 클러스터 추가
        clusters.add(createCluster(clusterStart, clusterEnd, clusterBuckets, bucketMap))

        return clusters
    }

    /**
     * 클러스터 생성
     */
    private fun createCluster(
        startIndex: Long,
        endIndex: Long,
        buckets: List<BucketData>,
        bucketMap: Map<Long, BucketData>
    ): HighlightCluster {
        // 클러스터 범위 내 모든 버킷 수집
        val allClusterBuckets = (startIndex..endIndex).mapNotNull { bucketMap[it] }
        val totalChatCount = allClusterBuckets.sumOf { it.chatCount.toInt() }
        val peakBucket = allClusterBuckets.maxByOrNull { it.chatCount } ?: buckets.first()
        val maxZScore = allClusterBuckets.maxOfOrNull { it.zScore } ?: 0.0

        return HighlightCluster(
            startBucketIndex = startIndex,
            endBucketIndex = endIndex,
            buckets = allClusterBuckets,
            totalChatCount = totalChatCount,
            peakBucketIndex = peakBucket.bucketIndex,
            peakChatCount = peakBucket.chatCount,
            maxZScore = maxZScore
        )
    }
}
