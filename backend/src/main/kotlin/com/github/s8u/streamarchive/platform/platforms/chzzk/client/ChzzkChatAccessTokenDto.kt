package com.github.s8u.streamarchive.platform.platforms.chzzk.client

data class ChzzkChatAccessTokenDto(
    val accessToken: String?,
    val temporaryRestrict: ChzzkTemporaryRestrictDto?,
    val realNameAuth: Boolean?,
    val extraToken: String?
)

data class ChzzkTemporaryRestrictDto(
    val temporaryRestrict: Boolean,
    val times: Int,
    val duration: Int?,
    val createdTime: Long?
)
