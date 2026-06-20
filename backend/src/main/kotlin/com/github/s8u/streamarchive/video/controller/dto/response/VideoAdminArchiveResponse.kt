package com.github.s8u.streamarchive.video.controller.dto.response

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAdminArchiveResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "동영상 소장 여부 설정 응답 (관리자)")
data class VideoAdminArchiveResponse(
    @field:Schema(description = "동영상 ID", example = "1")
    val id: Long,

    @field:Schema(description = "동영상 UUID")
    val uuid: String,

    @field:Schema(description = "채널 정보")
    val channel: ChannelInfo,

    @field:Schema(description = "제목", example = "오늘의 방송")
    val title: String,

    @field:Schema(description = "설명")
    val description: String?,

    @field:Schema(description = "재생 시간 (초)", example = "3600")
    val duration: Int,

    @field:Schema(description = "파일 크기 (바이트)", example = "1073741824")
    val fileSize: Long,

    @field:Schema(description = "썸네일 URL")
    val thumbnailUrl: String,

    @field:Schema(description = "HLS 플레이리스트 URL")
    val playlistUrl: String,

    @field:Schema(description = "콘텐츠 공개 범위 (PUBLIC/UNLISTED/PRIVATE)")
    val contentPrivacy: VideoContentPrivacy,

    @field:Schema(description = "채팅 싱크 오프셋 (밀리초)", example = "0")
    val chatSyncOffsetMillis: Long,

    @field:Schema(description = "소장 여부")
    val isArchived: Boolean,

    @field:Schema(description = "소장 처리 일시")
    val archivedAt: LocalDateTime?,

    @field:Schema(description = "소장 처리한 사용자 ID")
    val archivedBy: Long?,

    @field:Schema(description = "소장 처리 시 IP")
    val archivedIp: String?,

    @field:Schema(description = "생성 일시")
    val createdAt: LocalDateTime,

    @field:Schema(description = "수정 일시")
    val updatedAt: LocalDateTime,

    @field:Schema(description = "녹화 정보")
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
        fun from(result: VideoAdminArchiveResult): VideoAdminArchiveResponse {
            return VideoAdminArchiveResponse(
                id = result.id,
                uuid = result.uuid,
                channel = ChannelInfo(
                    id = result.channel.id,
                    uuid = result.channel.uuid,
                    name = result.channel.name,
                    profileUrl = result.channel.profileUrl
                ),
                title = result.title,
                description = result.description,
                duration = result.duration,
                fileSize = result.fileSize,
                thumbnailUrl = result.thumbnailUrl,
                playlistUrl = result.playlistUrl,
                contentPrivacy = result.contentPrivacy,
                chatSyncOffsetMillis = result.chatSyncOffsetMillis,
                isArchived = result.isArchived,
                archivedAt = result.archivedAt,
                archivedBy = result.archivedBy,
                archivedIp = result.archivedIp,
                createdAt = result.createdAt,
                updatedAt = result.updatedAt,
                record = result.record?.let { record ->
                    RecordInfo(
                        id = record.id,
                        platformType = record.platformType,
                        platformStreamId = record.platformStreamId,
                        recordQuality = record.recordQuality,
                        isEnded = record.isEnded,
                        isCancelled = record.isCancelled,
                        startedAt = record.startedAt,
                        endedAt = record.endedAt
                    )
                }
            )
        }
    }
}
