package com.github.s8u.streamarchive.platform.platforms.soop.chat

/**
 * SOOP 채팅 패킷
 */
data class SoopChatPacketDto(
    val serviceCode: Int,
    val resultCode: Int,
    val packet: List<String>
)
