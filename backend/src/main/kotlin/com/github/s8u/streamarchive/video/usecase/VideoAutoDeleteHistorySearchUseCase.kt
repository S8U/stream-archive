package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.video.entity.VideoAutoDeleteHistory
import com.github.s8u.streamarchive.video.repository.VideoAutoDeleteHistoryRepository
import com.github.s8u.streamarchive.video.usecase.dto.result.VideoAutoDeleteHistorySearchResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 자동 삭제 이력 조회 (관리자)
 *
 * 채널 ID가 없으면 전체 채널의 이력을 조회한다.
 */
@Service
class VideoAutoDeleteHistorySearchUseCase(
    private val videoAutoDeleteHistoryRepository: VideoAutoDeleteHistoryRepository,
    private val channelRepository: ChannelRepository
) {

    /**
     * 동영상 자동 삭제 이력을 조회한다.
     */
    @Transactional(readOnly = true)
    fun search(
        channelId: Long?,
        pageable: Pageable
    ): Page<VideoAutoDeleteHistorySearchResult> {
        val histories = if (channelId == null) {
            videoAutoDeleteHistoryRepository.findAllByOrderByIdDesc(pageable)
        } else {
            videoAutoDeleteHistoryRepository.findAllByChannelIdOrderByIdDesc(channelId, pageable)
        }

        val channelNames = findChannelNames(histories.content)

        return histories.map { history ->
            VideoAutoDeleteHistorySearchResult.from(
                history = history,
                channelName = channelNames[history.channelId] ?: ""
            )
        }
    }

    // 현재 페이지 이력들의 채널 이름을 한 번에 조회한다
    private fun findChannelNames(histories: List<VideoAutoDeleteHistory>): Map<Long, String> {
        val channelIds = histories.map { it.channelId }.distinct()
        return channelRepository.findAllById(channelIds).associate { it.id!! to it.name }
    }

}
