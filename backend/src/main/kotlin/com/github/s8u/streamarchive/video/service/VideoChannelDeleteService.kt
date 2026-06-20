package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.video.repository.VideoRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * 채널의 모든 동영상 삭제 서비스
 *
 * 소장된 동영상이 하나라도 있으면 삭제하지 않는다.
 * 소프트 삭제만 하고 삭제한 동영상 ID를 반환하며, 파일 삭제는 커밋 후 리스너가 처리한다.
 */
@Service
class VideoChannelDeleteService(
    private val videoRepository: VideoRepository
) {

    fun deleteAllByChannelId(channelId: Long): List<Long> {
        if (videoRepository.existsByChannelIdAndIsArchivedTrue(channelId)) {
            throw BusinessException("소장된 동영상이 있는 채널은 삭제할 수 없습니다. 소장 해제 후 삭제해주세요.", HttpStatus.CONFLICT)
        }

        val videos = videoRepository.findByChannelId(channelId)

        videos.forEach { video ->
            video.softDelete(userId = null, ip = null)
        }

        return videos.map { it.id!! }
    }

}
