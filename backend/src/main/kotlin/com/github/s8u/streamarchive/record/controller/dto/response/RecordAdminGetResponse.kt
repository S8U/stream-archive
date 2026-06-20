package com.github.s8u.streamarchive.record.controller.dto.response

import com.github.s8u.streamarchive.platform.enums.PlatformType
import com.github.s8u.streamarchive.record.usecase.dto.result.RecordAdminGetResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "녹화 기록 단건 응답 (관리자)")
data class RecordAdminGetResponse(
    @field:Schema(description = "녹화 기록 ID", example = "1")
    val id: Long,

    @field:Schema(description = "채널 정보")
    val channel: ChannelInfo,

    @field:Schema(description = "동영상 정보")
    val video: VideoInfo,

    @field:Schema(description = "플랫폼 유형 (CHZZK/SOOP/TWITCH/YOUTUBE)")
    val platformType: PlatformType,

    @field:Schema(description = "플랫폼 스트림 ID")
    val platformStreamId: String,

    @field:Schema(description = "녹화 화질")
    val recordQuality: String,

    @field:Schema(description = "종료 여부")
    val isEnded: Boolean,

    @field:Schema(description = "취소 여부")
    val isCancelled: Boolean,

    @field:Schema(description = "녹화 시작 일시")
    val createdAt: LocalDateTime,

    @field:Schema(description = "녹화 종료 일시")
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
        fun from(result: RecordAdminGetResult): RecordAdminGetResponse {
            return RecordAdminGetResponse(
                id = result.id,
                channel = ChannelInfo(
                    id = result.channel.id,
                    uuid = result.channel.uuid,
                    name = result.channel.name,
                    profileUrl = result.channel.profileUrl
                ),
                video = VideoInfo(
                    id = result.video.id,
                    uuid = result.video.uuid,
                    title = result.video.title,
                    thumbnailUrl = result.video.thumbnailUrl,
                    duration = result.video.duration
                ),
                platformType = result.platformType,
                platformStreamId = result.platformStreamId,
                recordQuality = result.recordQuality,
                isEnded = result.isEnded,
                isCancelled = result.isCancelled,
                createdAt = result.createdAt,
                endedAt = result.endedAt
            )
        }
    }
}
