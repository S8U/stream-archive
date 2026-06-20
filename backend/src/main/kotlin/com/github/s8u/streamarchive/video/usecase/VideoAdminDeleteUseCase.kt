package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.video.service.VideoDeleteService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 동영상 삭제 (관리자)
 */
@Service
class VideoAdminDeleteUseCase(
    private val videoDeleteService: VideoDeleteService
) {

    @Transactional
    fun delete(id: Long) {
        videoDeleteService.delete(id)
    }

}
