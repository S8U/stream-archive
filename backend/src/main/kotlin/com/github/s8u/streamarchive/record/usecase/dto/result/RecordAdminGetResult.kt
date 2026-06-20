package com.github.s8u.streamarchive.record.usecase.dto.result

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.entity.Record
import java.time.LocalDateTime

/**
 * 녹화 기록 단건 조회 결과 (관리자)
 */
data class RecordAdminGetResult(
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
        val thumbnailUrl: String,
        val duration: Int
    )

    companion object {
        fun from(
            record: Record,
            channelProfileUrl: String,
            videoThumbnailUrl: String
        ): RecordAdminGetResult {
            return RecordAdminGetResult(
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
                    thumbnailUrl = videoThumbnailUrl,
                    duration = record.video?.duration ?: 0
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
