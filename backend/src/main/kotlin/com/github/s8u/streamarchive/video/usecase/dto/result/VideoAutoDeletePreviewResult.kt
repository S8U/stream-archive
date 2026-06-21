package com.github.s8u.streamarchive.video.usecase.dto.result

import com.github.s8u.streamarchive.video.entity.Video
import java.time.LocalDateTime

/**
 * 동영상 자동 삭제 미리보기 목록 항목 결과
 *
 * 지금 정책대로라면 다음 자동 삭제에서 지워질 동영상 한 건이다.
 */
data class VideoAutoDeletePreviewResult(
    val id: Long,
    val uuid: String,
    val channelId: Long,
    val channelName: String,
    val title: String,
    val fileSize: Long,
    val thumbnailUrl: String,
    val createdAt: LocalDateTime,
    val ageDays: Int,
    val overDays: Int
) {

    companion object {
        fun from(
            video: Video,
            channelName: String,
            thumbnailUrl: String,
            ageDays: Int,
            overDays: Int
        ): VideoAutoDeletePreviewResult {
            return VideoAutoDeletePreviewResult(
                id = video.id!!,
                uuid = video.uuid,
                channelId = video.channelId,
                channelName = channelName,
                title = video.title,
                fileSize = video.fileSize,
                thumbnailUrl = thumbnailUrl,
                createdAt = video.createdAt,
                ageDays = ageDays,
                overDays = overDays
            )
        }
    }

}
