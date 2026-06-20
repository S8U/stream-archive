package com.github.s8u.streamarchive.recording.usecase.dto.command

import com.github.s8u.streamarchive.record.enums.RecordQuality
import com.github.s8u.streamarchive.platform.strategy.dto.PlatformStreamDto

data class RecordingStartCommand(
    val channelId: Long,
    val stream: PlatformStreamDto,
    val recordQuality: RecordQuality,
    val autoArchive: Boolean
)
