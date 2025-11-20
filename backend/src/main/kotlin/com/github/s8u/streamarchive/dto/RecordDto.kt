package com.github.s8u.streamarchive.dto

import com.github.s8u.streamarchive.entity.Record
import com.github.s8u.streamarchive.enums.PlatformType
import java.time.LocalDateTime

data class AdminRecordSearchRequest(
    val channelName: String? = null,
    val platformType: PlatformType? = null,
    val isEnded: Boolean? = null,
    val isCancelled: Boolean? = null,
    val createdAtFrom: LocalDateTime? = null,
    val createdAtTo: LocalDateTime? = null
)

data class AdminRecordResponse(
    val id: Long,
    val channelId: Long,
    val videoId: Long,
    val platformType: PlatformType,
    val platformStreamId: String,
    val recordQuality: String,
    val isEnded: Boolean,
    val isCancelled: Boolean,
    val createdAt: LocalDateTime,
    val endedAt: LocalDateTime?
) {
    companion object {
        fun from(record: Record): AdminRecordResponse {
            return AdminRecordResponse(
                id = record.id!!,
                channelId = record.channelId,
                videoId = record.videoId,
                platformType = record.platformType,
                platformStreamId = record.platformStreamId,
                recordQuality = record.recordQuality,
                isEnded = record.isEnded,
                isCancelled = record.isCancelled,
                createdAt = record.createdAt,
                endedAt = record.endedAt
            )
        }
    }
}
