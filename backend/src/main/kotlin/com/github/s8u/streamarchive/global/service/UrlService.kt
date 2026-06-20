package com.github.s8u.streamarchive.global.service

import com.github.s8u.streamarchive.global.properties.UrlProperties
import org.springframework.stereotype.Component

/**
 * 외부 노출용 API URL을 만든다.
 */
@Component
class UrlService(
    private val urlProperties: UrlProperties
) {

    // 채널 URL
    fun channelProfileUrl(channelUuid: String): String {
        return "${urlProperties.apiBase}/channels/$channelUuid/profile"
    }

    // 동영상 URL
    fun videoThumbnailUrl(videoUuid: String): String {
        return "${urlProperties.apiBase}/videos/$videoUuid/thumbnail"
    }

    fun videoPlaylistUrl(videoUuid: String): String {
        return "${urlProperties.apiBase}/videos/$videoUuid/playlist.m3u8"
    }

    fun videoSegmentUrl(videoUuid: String, filename: String): String {
        return "${urlProperties.apiBase}/videos/$videoUuid/$filename"
    }

}
