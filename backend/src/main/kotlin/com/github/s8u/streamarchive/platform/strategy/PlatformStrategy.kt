package com.github.s8u.streamarchive.platform.strategy

import com.github.s8u.streamarchive.platform.strategy.dto.PlatformChannelDto
import com.github.s8u.streamarchive.platform.strategy.dto.PlatformStreamDto
import com.github.s8u.streamarchive.platform.enums.PlatformType

interface PlatformStrategy {

    val platformType: PlatformType

    fun getStreamUrl(username: String): String

    fun getChannel(username: String): PlatformChannelDto?

    fun getStream(username: String): PlatformStreamDto?

    fun getStreamlinkArgs(): List<String> = emptyList()

}
