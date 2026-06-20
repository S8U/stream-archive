package com.github.s8u.streamarchive.recordschedule.service

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.recordschedule.repository.RecordScheduleRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * 채널에 속한 녹화 스케줄 삭제
 *
 * 채널 삭제 시 그 채널의 모든 녹화 스케줄을 함께 소프트 삭제한다.
 * 트랜잭션은 호출하는 UseCase가 소유한다.
 */
@Service
class RecordScheduleDeleteService(
    private val recordScheduleRepository: RecordScheduleRepository,
    private val channelRepository: ChannelRepository
) {

    fun deleteAllByChannelId(channelId: Long) {
        val channel = channelRepository.findById(channelId).orElseThrow {
            BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
        val recordSchedules = recordScheduleRepository.findByChannel(channel)

        recordSchedules.forEach { recordSchedule ->
            recordSchedule.softDelete(userId = null, ip = null)
        }
    }

}
