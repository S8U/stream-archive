package com.github.s8u.streamarchive.channel.usecase.dto.result

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import java.time.LocalDateTime

/**
 * 채널 상세 조회 결과 (관리자)
 */
data class ChannelAdminGetResult(
    val id: Long,
    val uuid: String,
    val name: String,
    val profileUrl: String,
    val totalVideoFileSize: Long,
    val contentPrivacy: ChannelContentPrivacy,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {

    companion object {
        fun from(channel: Channel, profileUrl: String, totalVideoFileSize: Long = 0): ChannelAdminGetResult {
            return ChannelAdminGetResult(
                id = channel.id!!,
                uuid = channel.uuid,
                name = channel.name,
                profileUrl = profileUrl,
                totalVideoFileSize = totalVideoFileSize,
                contentPrivacy = channel.contentPrivacy,
                createdAt = channel.createdAt,
                updatedAt = channel.updatedAt
            )
        }
    }
}
