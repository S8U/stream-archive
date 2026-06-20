package com.github.s8u.streamarchive.video.usecase.dto.command

import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy

data class VideoAdminUpdateCommand(
    val title: String? = null,
    val description: String? = null,
    val contentPrivacy: VideoContentPrivacy? = null,
    val chatSyncOffsetMillis: Long? = null
)
