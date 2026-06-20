package com.github.s8u.streamarchive.channel.usecase.dto.result

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import java.time.LocalDateTime

/**
 * 채널 목록 조회 결과 (공개)
 */
data class ChannelSearchResult(
    val uuid: String,
    val name: String,
    val profileUrl: String,
    val contentPrivacy: ChannelContentPrivacy,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {

    companion object {
        fun from(channel: Channel, profileUrl: String): ChannelSearchResult {
            return ChannelSearchResult(
                uuid = channel.uuid,
                name = channel.name,
                profileUrl = profileUrl,
                contentPrivacy = channel.contentPrivacy,
                createdAt = channel.createdAt,
                updatedAt = channel.updatedAt
            )
        }
    }
}
