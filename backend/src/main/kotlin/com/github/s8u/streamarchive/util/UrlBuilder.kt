package com.github.s8u.streamarchive.util

import com.github.s8u.streamarchive.properties.ApiProperties
import org.springframework.stereotype.Component

@Component
class UrlBuilder(
    private val apiProperties: ApiProperties
) {
    // Channel URLs
    fun channelProfileUrl(channelUuid: String): String {
        return "${apiProperties.baseUrl}/channels/$channelUuid/profile"
    }

    // Video URLs
    fun videoThumbnailUrl(videoUuid: String): String {
        return "${apiProperties.baseUrl}/videos/$videoUuid/thumbnail"
    }

    fun videoPlaylistUrl(videoUuid: String): String {
        return "${apiProperties.baseUrl}/videos/$videoUuid/playlist.m3u8"
    }

    fun videoSegmentUrl(videoUuid: String, filename: String): String {
        return "${apiProperties.baseUrl}/videos/$videoUuid/$filename"
    }
}