package com.github.s8u.streamarchive.client.chzzk

data class ChzzkLiveDetailDto(
    val liveId: Long?,
    val liveTitle: String?,
    val status: String,
    val liveImageUrl: String?,
    val defaultThumbnailImageUrl: String?,
    val concurrentUserCount: Int?,
    val accumulateCount: Int?,
    val openDate: String?,
    val closeDate: String?,
    val adult: Boolean?,
    val tags: List<String>?,
    val categoryType: String?,
    val liveCategory: String?,
    val liveCategoryValue: String?,
    val channel: ChzzkChannelDto?,
    val livePlaybackJson: String?
)