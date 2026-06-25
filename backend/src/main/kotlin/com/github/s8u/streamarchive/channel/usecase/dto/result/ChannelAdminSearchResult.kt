package com.github.s8u.streamarchive.channel.usecase.dto.result

import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.channel.repository.dto.ChannelAdminSearchProjection
import java.time.LocalDateTime

/**
 * 채널 목록 조회 결과 (관리자)
 */
data class ChannelAdminSearchResult(
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
        fun from(channel: ChannelAdminSearchProjection, profileUrl: String): ChannelAdminSearchResult {
            return ChannelAdminSearchResult(
                id = channel.id,
                uuid = channel.uuid,
                name = channel.name,
                profileUrl = profileUrl,
                totalVideoFileSize = channel.totalVideoFileSize,
                contentPrivacy = channel.contentPrivacy,
                createdAt = channel.createdAt,
                updatedAt = channel.updatedAt
            )
        }
    }
}
