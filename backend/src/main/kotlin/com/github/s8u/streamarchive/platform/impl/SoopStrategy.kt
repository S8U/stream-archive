package com.github.s8u.streamarchive.platform.impl

import com.github.s8u.streamarchive.client.soop.SoopApiClient
import com.github.s8u.streamarchive.enums.PlatformType
import com.github.s8u.streamarchive.platform.PlatformChannelDto
import com.github.s8u.streamarchive.platform.PlatformStrategy
import com.github.s8u.streamarchive.platform.PlatformStreamDto
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class SoopStrategy(
    private val apiClient: SoopApiClient
) : PlatformStrategy {

    override val platformType: PlatformType
        get() = PlatformType.SOOP

    override fun getStreamUrl(username: String): String {
        return "https://play.sooplive.co.kr/$username"
    }

    override fun getChannel(username: String): PlatformChannelDto? {
        val response = apiClient.getStation(username) ?: return null
        val station = response.station ?: return null

        return PlatformChannelDto(
            platformDto = station,
            platformType = platformType,
            id = station.userId,
            username = station.userId,
            name = station.userNick,
            thumbnailUrl = response.profileImage?.let {
                if (it.startsWith("//")) "https:$it" else it
            }
        )
    }

    override fun getStream(username: String): PlatformStreamDto? {
        val response = apiClient.getLiveDetail(username) ?: return null

        if (response.channel == null || response.channel.result != 1) {
            return null
        }

        val channel = response.channel

        // BNO가 없으면 방송 중이 아님
        if (channel.bno == null) {
            return null
        }

        // Station API로 추가 정보 조회 (방송 시작 시간 및 시청자 수)
        val stationResponse = apiClient.getStation(username)
        val broadStart = stationResponse?.station?.broadStart
        val viewerCount = stationResponse?.broad?.currentSumViewer

        return PlatformStreamDto(
            platformDto = channel,
            platformType = platformType,
            id = channel.bno,
            username = username,
            title = channel.title,
            category = channel.cate,
            viewerCount = viewerCount,
            thumbnailUrl = "https://liveimg.sooplive.co.kr/h/${channel.bno}.webp",
            startedAt = broadStart?.let {
                try {
                    LocalDateTime.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                } catch (e: Exception) {
                    LocalDateTime.now()
                }
            } ?: LocalDateTime.now()
        )
    }

}
