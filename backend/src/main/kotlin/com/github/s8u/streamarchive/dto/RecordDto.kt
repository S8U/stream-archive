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
    val channel: ChannelInfo,
    val video: VideoInfo,
    val platformType: PlatformType,
    val platformStreamId: String,
    val recordQuality: String,
    val isEnded: Boolean,
    val isCancelled: Boolean,
    val createdAt: LocalDateTime,
    val endedAt: LocalDateTime?
) {
    data class ChannelInfo(
        val id: Long,
        val uuid: String,
        val name: String,
        val profileUrl: String
    )

    data class VideoInfo(
        val id: Long,
        val uuid: String,
        val title: String,
        val thumbnailUrl: String
    )

    companion object {
        fun from(
            record: Record,
            channelProfileUrl: String,
            videoThumbnailUrl: String
        ): AdminRecordResponse {
            return AdminRecordResponse(
                id = record.id!!,
                channel = ChannelInfo(
                    id = record.channel?.id!!,
                    uuid = record.channel?.uuid!!,
                    name = record.channel?.name!!,
                    profileUrl = channelProfileUrl
                ),
                video = VideoInfo(
                    id = record.video?.id!!,
                    uuid = record.video?.uuid!!,
                    title = record.video?.title!!,
                    thumbnailUrl = videoThumbnailUrl
                ),
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
