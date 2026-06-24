package com.github.s8u.streamarchive.platform.platforms.youtube.service.dto

import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeVideo

/**
 * 유튜브 라이브 조회 결과
 *
 * 라이브 중인 동영상과 그 채널 ID를 함께 담는다.
 */
data class YoutubeLiveFindResult(
    val channelId: String,
    val video: YoutubeVideo
)
