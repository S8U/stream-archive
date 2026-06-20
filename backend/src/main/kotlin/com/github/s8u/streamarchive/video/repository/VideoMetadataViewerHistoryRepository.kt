package com.github.s8u.streamarchive.video.repository

import com.github.s8u.streamarchive.video.entity.VideoMetadataViewerHistory
import com.github.s8u.streamarchive.video.repository.dto.VideoPeakViewerProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface VideoMetadataViewerHistoryRepository : JpaRepository<VideoMetadataViewerHistory, Long> {

    fun findByVideoIdOrderByOffsetMillisAsc(videoId: Long): List<VideoMetadataViewerHistory>

    fun findTopByVideoIdOrderByViewerCountDescOffsetMillisAsc(videoId: Long): VideoMetadataViewerHistory?

    @Query("""
        SELECT new com.github.s8u.streamarchive.video.repository.dto.VideoPeakViewerProjection(
            h.videoId, MAX(h.viewerCount)
        )
        FROM VideoMetadataViewerHistory h
        WHERE h.videoId IN :videoIds
        GROUP BY h.videoId
    """)
    fun findPeakViewerCountsByVideoIds(@Param("videoIds") videoIds: List<Long>): List<VideoPeakViewerProjection>

}
