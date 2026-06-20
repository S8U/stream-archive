package com.github.s8u.streamarchive.video.controller.dto.response

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoSearchResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "동영상 목록 응답 (공개)")
data class VideoSearchResponse(
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

    @field:Schema(description = "채팅 싱크 오프셋 (밀리초)", example = "0")
    val chatSyncOffsetMillis: Long,

    @field:Schema(description = "소장 여부")
    val isArchived: Boolean,

    @field:Schema(description = "최고 시청자 수", example = "1234")
    val peakViewerCount: Int?,

    @field:Schema(description = "생성 일시")
    val createdAt: LocalDateTime,

    @field:Schema(description = "녹화 정보")
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
        fun from(result: VideoSearchResult): VideoSearchResponse {
            return VideoSearchResponse(
                uuid = result.uuid,
                channel = ChannelInfo(
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
                chatSyncOffsetMillis = result.chatSyncOffsetMillis,
                isArchived = result.isArchived,
                peakViewerCount = result.peakViewerCount,
                createdAt = result.createdAt,
                record = result.record?.let { record ->
                    RecordInfo(
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
