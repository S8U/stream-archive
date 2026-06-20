package com.github.s8u.streamarchive.video.repository

import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.repository.dto.VideoDailyStatProjection
import com.github.s8u.streamarchive.video.usecase.dto.command.VideoAdminSearchCommand
import com.github.s8u.streamarchive.video.usecase.dto.command.VideoSearchCommand
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface VideoRepositoryCustom {

    fun searchForAdmin(command: VideoAdminSearchCommand, pageable: Pageable): Page<Video>
    fun searchForPublic(command: VideoSearchCommand, pageable: Pageable): Page<Video>

    // 대시보드 통계
    fun sumDuration(): Long?
    fun sumFileSize(): Long?
    fun getDailyVideoStats(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<VideoDailyStatProjection>

    // 채널별 통계
    fun countByChannelId(channelId: Long): Long
    fun sumFileSizeByChannelId(channelId: Long): Long
    fun sumFileSizeByChannelIds(channelIds: Collection<Long>): Map<Long, Long>

}
