package com.github.s8u.streamarchive.platform.platforms.youtube.service

import com.github.s8u.streamarchive.platform.platforms.youtube.client.YoutubeApiClient
import com.github.s8u.streamarchive.platform.platforms.youtube.client.dto.YoutubeVideo
import com.github.s8u.streamarchive.platform.platforms.youtube.service.dto.YoutubeLiveFindResult
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * 유튜브 라이브 조회
 *
 * 업로드 재생목록의 최신 동영상 중 진행 중인 라이브를 찾는다.
 * 한 번 찾은 라이브 동영상 ID는 캐싱해, 다음 조회부터 재생목록 호출을 건너뛴다.
 */
@Service
class YoutubeLiveFindService(
    private val apiClient: YoutubeApiClient
) {

    // 채널 ID -> 진행 중인 라이브 동영상 ID
    private val liveVideoIdCache = ConcurrentHashMap<String, String>()

    /**
     * 채널에서 진행 중인 라이브를 찾습니다.
     *
     * 라이브가 아니면 null을 반환한다.
     */
    fun find(channelId: String): YoutubeLiveFindResult? {
        // 캐싱된 동영상이 아직 라이브면 재생목록 조회를 건너뛴다
        val cachedVideoId = liveVideoIdCache[channelId]
        if (cachedVideoId != null) {
            val cachedVideo = getLiveVideo(listOf(cachedVideoId))
            if (cachedVideo != null) {
                return YoutubeLiveFindResult(channelId, cachedVideo)
            }
            liveVideoIdCache.remove(channelId)
        }

        // 업로드 재생목록의 최신 동영상 중 라이브를 찾는다
        val videoIds = getRecentVideoIds(channelId)
        val liveVideo = getLiveVideo(videoIds) ?: return null

        liveVideoIdCache[channelId] = liveVideo.id
        return YoutubeLiveFindResult(channelId, liveVideo)
    }

    // 재생목록 조회는 1 unit이라, 100 unit인 search.list 대신 쓴다
    private fun getRecentVideoIds(channelId: String): List<String> {
        val response = apiClient.getPlaylistItems(
            playlistId = toUploadsPlaylistId(channelId),
            maxResults = RECENT_VIDEO_COUNT
        ) ?: return emptyList()

        return response.items.map { it.contentDetails.videoId }
    }

    private fun getLiveVideo(videoIds: List<String>): YoutubeVideo? {
        if (videoIds.isEmpty()) {
            return null
        }

        return apiClient.getVideos(videoIds)
            ?.items
            ?.firstOrNull { isLive(it) }
    }

    private fun isLive(video: YoutubeVideo): Boolean {
        // 라이브가 시작됐고 아직 끝나지 않았으면 진행 중이다
        val details = video.liveStreamingDetails
        return video.snippet?.liveBroadcastContent == LIVE_BROADCAST_CONTENT &&
            details?.actualStartTime != null &&
            details.actualEndTime == null
    }

    // 채널 ID(UC...)의 업로드 재생목록 ID는 두 번째 글자만 U로 바꾼 값이다
    private fun toUploadsPlaylistId(channelId: String): String {
        return "UU" + channelId.removePrefix("UC")
    }

    companion object {
        private const val RECENT_VIDEO_COUNT = 5
        private const val LIVE_BROADCAST_CONTENT = "live"
    }

}
