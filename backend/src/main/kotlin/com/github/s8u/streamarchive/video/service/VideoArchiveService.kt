package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.entity.VideoArchiveHistory
import com.github.s8u.streamarchive.video.repository.VideoArchiveHistoryRepository
import org.springframework.stereotype.Service

/**
 * 동영상 소장 서비스
 */
@Service
class VideoArchiveService(
    private val videoArchiveHistoryRepository: VideoArchiveHistoryRepository
) {

    /**
     * 동영상의 소장 여부를 설정하고 이력을 남긴다.
     *
     * 자동 소장(녹화 시작)은 [userId]/[ip] 없이 호출한다.
     */
    fun setArchived(video: Video, isArchived: Boolean, userId: Long?, ip: String?) {
        if (isArchived) {
            video.archive(userId, ip)
        } else {
            video.unarchive()
        }

        videoArchiveHistoryRepository.save(
            VideoArchiveHistory(
                videoId = video.id!!,
                isArchived = isArchived,
                actionBy = userId,
                actionIp = ip
            )
        )
    }

}
