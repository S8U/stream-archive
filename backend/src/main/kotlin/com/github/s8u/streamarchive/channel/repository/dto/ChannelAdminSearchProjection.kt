package com.github.s8u.streamarchive.channel.repository.dto

import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import java.time.LocalDateTime

data class ChannelAdminSearchProjection(
    val id: Long,
    val uuid: String,
    val name: String,
    val totalVideoFileSize: Long,
    val contentPrivacy: ChannelContentPrivacy,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
