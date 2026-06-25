package com.github.s8u.streamarchive.video.repository

import com.github.s8u.streamarchive.video.entity.VideoAutoDeleteHistory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface VideoAutoDeleteHistoryRepository : JpaRepository<VideoAutoDeleteHistory, Long> {

    fun findAllByOrderByIdDesc(pageable: Pageable): Page<VideoAutoDeleteHistory>

    fun findAllByChannelIdOrderByIdDesc(
        channelId: Long,
        pageable: Pageable
    ): Page<VideoAutoDeleteHistory>

}
