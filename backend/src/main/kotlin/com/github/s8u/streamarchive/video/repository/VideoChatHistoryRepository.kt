package com.github.s8u.streamarchive.video.repository

import com.github.s8u.streamarchive.video.entity.VideoChatHistory
import com.github.s8u.streamarchive.video.repository.dto.VideoChatMessageProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface VideoChatHistoryRepository : JpaRepository<VideoChatHistory, Long> {

    fun findByVideoIdAndOffsetMillisGreaterThanEqualAndOffsetMillisLessThanOrderByOffsetMillisAsc(
        videoId: Long,
        startOffset: Long,
        endOffset: Long
    ): List<VideoChatHistory>

    /**
     * 동영상의 전체 채팅을 오프셋 순으로 조회한다(분석용).
     *
     * 분석에 필요한 오프셋·메시지만 투영해 엔티티 전체 로드를 피한다.
     */
    @Query(
        """
        SELECT new com.github.s8u.streamarchive.video.repository.dto.VideoChatMessageProjection(
            h.offsetMillis, h.message, h.emojis
        )
        FROM VideoChatHistory h
        WHERE h.videoId = :videoId
        ORDER BY h.offsetMillis ASC
        """
    )
    fun findMessagesByVideoId(@Param("videoId") videoId: Long): List<VideoChatMessageProjection>

}
