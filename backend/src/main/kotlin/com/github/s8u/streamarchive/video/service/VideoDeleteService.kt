package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.video.event.VideoDeletedEvent
import com.github.s8u.streamarchive.video.repository.VideoRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * 동영상 삭제 서비스
 *
 * 동영상을 소프트 삭제하고 삭제 이벤트를 발행한다.
 * 소장된 동영상은 삭제할 수 없다.
 * 파일 삭제는 커밋 후 리스너가 처리한다.
 * 여러 진입점(관리자 삭제, 녹화 종료 시 짧은 영상 정리)이 공유한다.
 */
@Service
class VideoDeleteService(
    private val videoRepository: VideoRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    /**
     * 동영상을 삭제한다.
     */
    fun delete(id: Long) {
        val video = videoRepository.findById(id).orElseThrow {
            BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        if (video.isArchived) {
            throw BusinessException(
                "소장된 동영상은 삭제할 수 없습니다. 소장 해제 후 삭제해주세요.",
                HttpStatus.CONFLICT
            )
        }

        video.softDelete(userId = null, ip = null)

        eventPublisher.publishEvent(VideoDeletedEvent(video.id!!))
    }

}
