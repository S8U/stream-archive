package com.github.s8u.streamarchive.dto

import com.github.s8u.streamarchive.entity.Video
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.github.s8u.streamarchive.enums.PlatformType
import java.time.LocalDateTime

data class AdminVideoUpdateRequest(
    val title: String?,
    val contentPrivacy: ContentPrivacy?
)

data class AdminVideoSearchRequest(
    val title: String? = null,
    val channelName: String? = null,
    val contentPrivacy: ContentPrivacy? = null,
    val createdAtFrom: LocalDateTime? = null,
    val createdAtTo: LocalDateTime? = null
)

data class AdminVideoResponse(
    val id: Long,
    val uuid: String,
    val channelId: Long,
    val title: String,
    val duration: Int,
    val fileSize: Long,
    val thumbnailUrl: String,
    val playlistUrl: String,
    val contentPrivacy: ContentPrivacy,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(
            video: Video,
            thumbnailUrl: String,
            playlistUrl: String
        ): AdminVideoResponse {
            return AdminVideoResponse(
                id = video.id!!,
                uuid = video.uuid,
                channelId = video.channelId,
                title = video.title,
                duration = video.duration,
                fileSize = video.fileSize,
                thumbnailUrl = thumbnailUrl,
                playlistUrl = playlistUrl,
                contentPrivacy = video.contentPrivacy,
                createdAt = video.createdAt,
                updatedAt = video.updatedAt
            )
        }
    }
}

data class PublicVideoSearchRequest(
    val title: String? = null,
    val channelName: String? = null,
    val channelUuid: String? = null
)

data class PublicVideoResponse(
    val uuid: String,
    val channel: ChannelInfo,
    val title: String,
    val duration: Int,
    val fileSize: Long,
    val thumbnailUrl: String,
    val playlistUrl: String,
    val createdAt: LocalDateTime,
    val record: RecordInfo?
) {
    data class ChannelInfo(
        val uuid: String,
        val name: String,
        val profileUrl: String
    )

    data class RecordInfo(
        val platformType: PlatformType,
        val platformStreamId: String,
        val recordQuality: String,
        val isEnded: Boolean,
        val isCancelled: Boolean,
        val startedAt: LocalDateTime,
        val endedAt: LocalDateTime?
    )

    companion object {
        fun from(
            video: Video,
            channelProfileUrl: String,
            thumbnailUrl: String,
            playlistUrl: String
        ): PublicVideoResponse {
            val channel = video.channel!!

            return PublicVideoResponse(
                uuid = video.uuid,
                channel = ChannelInfo(
                    uuid = channel.uuid,
                    name = channel.name,
                    profileUrl = channelProfileUrl
                ),
                title = video.title,
                duration = video.duration,
                fileSize = video.fileSize,
                thumbnailUrl = thumbnailUrl,
                playlistUrl = playlistUrl,
                createdAt = video.createdAt,
                record = video.record?.let { record ->
                    RecordInfo(
                        platformType = record.platformType,
                        platformStreamId = record.platformStreamId,
                        recordQuality = record.recordQuality,
                        isEnded = record.isEnded,
                        isCancelled = record.isCancelled,
                        startedAt = record.createdAt,
                        endedAt = record.endedAt
                    )
                }
            )
        }
    }
}
