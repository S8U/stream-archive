package com.github.s8u.streamarchive.platform.platforms.youtube.service

import com.github.s8u.streamarchive.platform.platforms.youtube.client.YoutubeApiClient
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeChannelItem
import org.springframework.stereotype.Service

/**
 * 유튜브 채널 조회
 *
 * 입력이 채널 ID인지 핸들인지 가려 알맞은 API로 조회한다.
 * Strategy와 ChatStrategy가 같은 방식으로 채널을 찾도록 한 곳에 모은다.
 */
@Service
class YoutubeChannelFindService(
    private val apiClient: YoutubeApiClient
) {

    /**
     * 입력값으로 유튜브 채널을 찾습니다.
     *
     * 채널을 찾지 못하면 null을 반환한다.
     */
    fun find(input: String): YoutubeChannelItem? {
        val normalized = normalize(input)

        val response = if (isChannelId(normalized)) {
            apiClient.getChannel(normalized)
        } else {
            apiClient.getChannelByHandle(normalized)
        }

        return response?.items?.firstOrNull()
    }

    /**
     * 입력값에서 핸들의 `@`를 떼고 공백을 정리합니다.
     */
    fun normalize(input: String): String {
        return input
            .trim()
            .removePrefix("@")
    }

    /**
     * 정규화된 입력값이 유튜브 채널 ID인지 판단합니다.
     *
     * 채널 ID는 24자 `UC`로 시작하는 형식이다.
     * `@`를 뗀 핸들(UCLA 등)과 섞이지 않도록 길이까지 확인한다.
     */
    fun isChannelId(normalized: String): Boolean {
        return normalized.startsWith("UC") && normalized.length == CHANNEL_ID_LENGTH
    }

    companion object {
        private const val CHANNEL_ID_LENGTH = 24
    }

}
