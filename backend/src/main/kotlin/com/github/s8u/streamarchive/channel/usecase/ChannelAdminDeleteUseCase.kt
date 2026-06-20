package com.github.s8u.streamarchive.channel.usecase

import com.github.s8u.streamarchive.channel.event.ChannelDeletedEvent
import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.channelplatform.service.ChannelPlatformDeleteService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.recordschedule.service.RecordScheduleDeleteService
import com.github.s8u.streamarchive.video.service.VideoChannelDeleteService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널 삭제 (관리자)
 *
 * 녹화 스케줄, 채널 플랫폼, 동영상을 연쇄 삭제한 뒤 채널을 소프트 삭제한다.
 * 파일 삭제(프로필·동영상)는 커밋 후 리스너가 처리한다.
 */
@Service
class ChannelAdminDeleteUseCase(
    private val channelRepository: ChannelRepository,
    private val recordScheduleDeleteService: RecordScheduleDeleteService,
    private val channelPlatformDeleteService: ChannelPlatformDeleteService,
    private val videoChannelDeleteService: VideoChannelDeleteService,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun delete(id: Long) {
        val channel = channelRepository.findById(id).orElseThrow {
            BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        // 모든 녹화 스케줄 삭제
        recordScheduleDeleteService.deleteAllByChannelId(id)

        // 모든 채널 플랫폼 삭제
        channelPlatformDeleteService.deleteAllByChannelId(id)

        // 모든 동영상 삭제
        val deletedVideoIds = videoChannelDeleteService.deleteAllByChannelId(id)

        // 채널 삭제
        channel.softDelete(userId = null, ip = null)

        eventPublisher.publishEvent(ChannelDeletedEvent(channelId = id, videoIds = deletedVideoIds))
    }

}
