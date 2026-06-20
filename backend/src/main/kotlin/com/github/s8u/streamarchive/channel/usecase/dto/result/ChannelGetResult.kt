package com.github.s8u.streamarchive.channel.usecase.dto.result

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import java.time.LocalDateTime

/**
 * 채널 단건 조회 결과 (공개)
 */
data class ChannelGetResult(
    val uuid: String,
    val name: String,
    val profileUrl: String,
    val contentPrivacy: ChannelContentPrivacy,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {

    companion object {
        fun from(channel: Channel, profileUrl: String): ChannelGetResult {
            return ChannelGetResult(
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
