package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.SaveWatchHistoryRequest
import com.github.s8u.streamarchive.dto.WatchHistoryListResponse
import com.github.s8u.streamarchive.dto.WatchHistoryResponse
import com.github.s8u.streamarchive.entity.UserVideoWatchHistory
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.UserVideoWatchHistoryRepository
import com.github.s8u.streamarchive.repository.VideoRepository
import com.github.s8u.streamarchive.util.UrlBuilder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WatchHistoryService(
    private val watchHistoryRepository: UserVideoWatchHistoryRepository,
    private val videoRepository: VideoRepository,
    private val authenticationService: AuthenticationService,
    private val urlBuilder: UrlBuilder
) {

    @Transactional(readOnly = true)
    fun getWatchHistory(videoUuid: String): WatchHistoryResponse? {
        val userId = authenticationService.getCurrentUserId()
            ?: throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        val video = videoRepository.findByUuid(videoUuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        val history = watchHistoryRepository.findByUserIdAndVideoId(userId, video.id!!)
        return history?.let { WatchHistoryResponse.from(it) }
    }

    @Transactional
    fun saveWatchHistory(videoUuid: String, request: SaveWatchHistoryRequest) {
        val userId = authenticationService.getCurrentUserId()
            ?: throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        val video = videoRepository.findByUuid(videoUuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        val existing = watchHistoryRepository.findByUserIdAndVideoId(userId, video.id!!)
        if (existing != null) {
            // 기존 기록 업데이트
            existing.lastPosition = request.position
        } else {
            // 새 기록 생성
            watchHistoryRepository.save(
                UserVideoWatchHistory(
                    userId = userId,
                    videoId = video.id!!,
                    lastPosition = request.position
                )
            )
        }
    }

    @Transactional(readOnly = true)
    fun getWatchHistories(pageable: Pageable): Page<WatchHistoryListResponse> {
        val userId = authenticationService.getCurrentUserId()
            ?: throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        return watchHistoryRepository.findByUserIdOrderByWatchedAtDesc(userId, pageable)
            .map { history ->
                val video = videoRepository.findById(history.videoId).orElseThrow {
                    BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
                }

                WatchHistoryListResponse.from(
                    history = history,
                    video = video,
                    channelProfileUrl = urlBuilder.channelProfileUrl(video.channel?.uuid!!),
                    videoThumbnailUrl = urlBuilder.videoThumbnailUrl(video.uuid)
                )
            }
    }

    @Transactional
    fun deleteWatchHistory(videoUuid: String) {
        val userId = authenticationService.getCurrentUserId()
            ?: throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        val video = videoRepository.findByUuid(videoUuid) ?: throw BusinessException(
            "동영상을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        watchHistoryRepository.deleteByUserIdAndVideoId(userId, video.id!!)
    }

    @Transactional
    fun deleteAllWatchHistories() {
        val userId = authenticationService.getCurrentUserId()
            ?: throw BusinessException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED)

        watchHistoryRepository.deleteAllByUserId(userId)
    }
}

