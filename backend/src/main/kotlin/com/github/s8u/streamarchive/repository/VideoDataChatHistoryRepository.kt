package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.VideoDataChatHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * 버킷별 채팅 카운트를 위한 Projection 인터페이스
 */
interface ChatCountBucket {
    fun getBucketIndex(): Long
    fun getChatCount(): Long
}

interface VideoDataChatHistoryRepository : JpaRepository<VideoDataChatHistory, Long> {
    fun findByVideoIdAndOffsetMillisGreaterThanEqualAndOffsetMillisLessThanOrderByOffsetMillisAsc(
        videoId: Long,
        startOffset: Long,
        endOffset: Long
    ): List<VideoDataChatHistory>

    /**
     * 시간 버킷별 채팅 카운트 조회 (하이라이트 분석용)
     */
    @Query(
        """
        SELECT 
            FLOOR(offset_millis / :bucketSizeMillis) as bucketIndex,
            COUNT(*) as chatCount
        FROM video_data_chat_histories
        WHERE video_id = :videoId
        GROUP BY FLOOR(offset_millis / :bucketSizeMillis)
        ORDER BY bucketIndex
        """,
        nativeQuery = true
    )
    fun countChatsByBucket(
        @Param("videoId") videoId: Long,
        @Param("bucketSizeMillis") bucketSizeMillis: Long
    ): List<ChatCountBucket>
}