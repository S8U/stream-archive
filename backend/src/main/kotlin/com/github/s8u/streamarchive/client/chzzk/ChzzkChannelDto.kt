package com.github.s8u.streamarchive.client.chzzk

data class ChzzkChannelDto(
    val channelId: String,
    val channelName: String,
    val channelImageUrl: String?,
    val verifiedMark: Boolean?,
    val channelType: String?,
    val channelDescription: String?,
    val followerCount: Long?,
    val openLive: Boolean?,
    val subscriptionAvailability: Boolean?
)