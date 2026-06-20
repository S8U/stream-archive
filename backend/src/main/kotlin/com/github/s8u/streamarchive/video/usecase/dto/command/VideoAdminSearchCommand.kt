package com.github.s8u.streamarchive.video.usecase.dto.command

import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import java.time.LocalDateTime

data class VideoAdminSearchCommand(
    val id: Long? = null,
    val uuid: String? = null,
    val title: String? = null,
    val description: String? = null,
    val channelName: String? = null,
    val contentPrivacy: VideoContentPrivacy? = null,
    val isArchived: Boolean? = null,
    val createdAtFrom: LocalDateTime? = null,
    val createdAtTo: LocalDateTime? = null
)
