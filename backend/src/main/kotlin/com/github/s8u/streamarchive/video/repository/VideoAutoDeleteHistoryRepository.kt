package com.github.s8u.streamarchive.video.repository

import com.github.s8u.streamarchive.video.entity.VideoAutoDeleteHistory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface VideoAutoDeleteHistoryRepository : JpaRepository<VideoAutoDeleteHistory, Long> {

    // 삭제일(생성 일시) 최신순으로 정렬한다 (같은 시각이면 동영상 ID 내림차순)
    fun findAllByOrderByCreatedAtDescVideoIdDesc(pageable: Pageable): Page<VideoAutoDeleteHistory>

    fun findAllByChannelIdOrderByCreatedAtDescVideoIdDesc(
        channelId: Long,
        pageable: Pageable
    ): Page<VideoAutoDeleteHistory>

}
