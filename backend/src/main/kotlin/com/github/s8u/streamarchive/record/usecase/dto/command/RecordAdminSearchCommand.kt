package com.github.s8u.streamarchive.record.usecase.dto.command

import com.github.s8u.streamarchive.platform.enums.PlatformType
import java.time.LocalDateTime

data class RecordAdminSearchCommand(
    val id: Long? = null,
    val channelName: String? = null,
    val title: String? = null,
    val platformStreamId: String? = null,
    val platformType: PlatformType? = null,
    val isEnded: Boolean? = null,
    val isCancelled: Boolean? = null,
    val createdAtFrom: LocalDateTime? = null,
    val createdAtTo: LocalDateTime? = null
)
