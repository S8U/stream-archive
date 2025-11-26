package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.Record
import com.github.s8u.streamarchive.enums.PlatformType
import org.springframework.data.jpa.repository.JpaRepository

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
}
