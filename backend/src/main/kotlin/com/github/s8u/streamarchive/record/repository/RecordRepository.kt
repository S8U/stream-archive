package com.github.s8u.streamarchive.record.repository

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.entity.Record
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface RecordRepository : JpaRepository<Record, Long>, RecordRepositoryCustom {

    fun existsByPlatformTypeAndPlatformStreamIdAndIsEndedAndIsCancelled(
        platformType: PlatformType,
        platformStreamId: String,
        isEnded: Boolean,
        isCancelled: Boolean
    ): Boolean

    fun existsByPlatformTypeAndPlatformStreamIdAndIsCancelled(
        platformType: PlatformType,
        platformStreamId: String,
        isCancelled: Boolean
    ): Boolean

    fun countByPlatformTypeAndPlatformStreamIdAndIsFailedAndCreatedAtAfter(
        platformType: PlatformType,
        platformStreamId: String,
        isFailed: Boolean,
        createdAt: LocalDateTime
    ): Long

    fun findByPlatformTypeAndPlatformStreamIdAndIsEndedAndIsCancelled(
        platformType: PlatformType,
        platformStreamId: String,
        isEnded: Boolean,
        isCancelled: Boolean
    ): Record?

    fun findByChannelId(channelId: Long): List<Record>

    fun findByVideoId(videoId: Long): List<Record>

    fun findAllByOrderByCreatedAtDesc(): List<Record>

    fun findByIsEndedFalseAndIsCancelledFalse(): List<Record>

    fun findByChannelIdAndPlatformTypeAndIsEndedAndIsCancelled(
        channelId: Long,
        platformType: PlatformType,
        isEnded: Boolean,
        isCancelled: Boolean
    ): Record?

}
