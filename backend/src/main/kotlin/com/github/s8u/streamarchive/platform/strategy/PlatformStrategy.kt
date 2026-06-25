package com.github.s8u.streamarchive.platform.strategy

import com.github.s8u.streamarchive.platform.strategy.dto.PlatformChannelDto
import com.github.s8u.streamarchive.platform.strategy.dto.PlatformStreamDto
import com.github.s8u.streamarchive.platform.enums.PlatformType

interface PlatformStrategy {

    val platformType: PlatformType

    fun getStreamUrl(username: String): String

    /**
     * 플랫폼 채널 URL에서 채널 ID를 뽑아낸다.
     *
     * 이 플랫폼의 URL이 아니면 null을 반환한다.
     * URL이 아닌 채널 ID 문자열을 그대로 넣어도 처리할지는 구현이 정한다.
     */
    fun parseChannelId(url: String): String?

    fun getChannel(username: String): PlatformChannelDto?

    fun getStream(username: String): PlatformStreamDto?

    fun getStreamlinkArgs(): List<String> = emptyList()

}
