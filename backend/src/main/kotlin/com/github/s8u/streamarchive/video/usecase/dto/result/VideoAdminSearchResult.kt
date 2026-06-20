package com.github.s8u.streamarchive.video.usecase.dto.result

import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.video.entity.Video
import java.time.LocalDateTime

/**
 * 동영상 목록 조회 결과 (관리자)
 */
data class VideoAdminSearchResult(
    val id: Long,
    val uuid: String,
    val channel: ChannelInfo,
    val title: String,
    val description: String?,
    val duration: Int,
    val fileSize: Long,
    val thumbnailUrl: String,
    val playlistUrl: String,
    val contentPrivacy: VideoContentPrivacy,
    val chatSyncOffsetMillis: Long,
    val isArchived: Boolean,
    val archivedAt: LocalDateTime?,
    val archivedBy: Long?,
    val archivedIp: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val record: RecordInfo?
) {

    data class ChannelInfo(
        val id: Long,
        val uuid: String,
        val name: String,
        val profileUrl: String
    )

    data class RecordInfo(
        val id: Long,
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
        ): VideoAdminSearchResult {
            return VideoAdminSearchResult(
                id = video.id!!,
                uuid = video.uuid,
                channel = ChannelInfo(
                    id = video.channel?.id!!,
                    uuid = video.channel?.uuid!!,
                    name = video.channel?.name!!,
                    profileUrl = channelProfileUrl
                ),
                title = video.title,
                description = video.description,
                duration = video.duration,
                fileSize = video.fileSize,
                thumbnailUrl = thumbnailUrl,
                playlistUrl = playlistUrl,
                contentPrivacy = video.contentPrivacy,
                chatSyncOffsetMillis = video.chatSyncOffsetMillis,
                isArchived = video.isArchived,
                archivedAt = video.archivedAt,
                archivedBy = video.archivedBy,
                archivedIp = video.archivedIp,
                createdAt = video.createdAt,
                updatedAt = video.updatedAt,
                record = video.record?.let { record ->
                    RecordInfo(
                        id = record.id!!,
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
