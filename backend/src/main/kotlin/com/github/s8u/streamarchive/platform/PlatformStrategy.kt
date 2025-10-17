package com.github.s8u.streamarchive.platform

import com.github.s8u.streamarchive.enums.PlatformType

interface PlatformStrategy {

    val platformType: PlatformType

    fun getStreamUrl(username: String): String

    fun getChannel(username: String): PlatformChannelDto?

    fun getStream(username: String): PlatformStreamDto?

}