package com.github.s8u.streamarchive.video.usecase.dto.result

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.video.entity.Video
import java.time.LocalDateTime

/**
 * 동영상 단건 조회 결과 (공개)
 */
data class VideoGetResult(
    val uuid: String,
    val channel: ChannelInfo,
    val title: String,
    val description: String?,
    val duration: Int,
    val fileSize: Long,
    val thumbnailUrl: String,
    val playlistUrl: String,
    val chatSyncOffsetMillis: Long,
    val isArchived: Boolean,
    val peakViewerCount: Int?,
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
            playlistUrl: String,
            peakViewerCount: Int?
        ): VideoGetResult {
            val channel = video.channel!!

            return VideoGetResult(
                uuid = video.uuid,
                channel = ChannelInfo(
                    uuid = channel.uuid,
                    name = channel.name,
                    profileUrl = channelProfileUrl
                ),
                title = video.title,
                description = video.description,
                duration = video.duration,
                fileSize = video.fileSize,
                thumbnailUrl = thumbnailUrl,
                playlistUrl = playlistUrl,
                chatSyncOffsetMillis = video.chatSyncOffsetMillis,
                isArchived = video.isArchived,
                peakViewerCount = peakViewerCount,
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
