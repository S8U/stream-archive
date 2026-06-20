package com.github.s8u.streamarchive.recordschedule.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.recordschedule.entity.RecordSchedule
import com.github.s8u.streamarchive.recordschedule.repository.RecordScheduleRepository
import com.github.s8u.streamarchive.recordschedule.usecase.dto.command.RecordScheduleAdminCreateCommand
import com.github.s8u.streamarchive.recordschedule.usecase.dto.result.RecordScheduleAdminCreateResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 녹화 스케줄 생성 (관리자)
 */
@Service
class RecordScheduleAdminCreateUseCase(
    private val recordScheduleRepository: RecordScheduleRepository,
    private val channelRepository: ChannelRepository,
    private val urlService: UrlService
) {

    @Transactional
    fun create(command: RecordScheduleAdminCreateCommand): RecordScheduleAdminCreateResult {
        val channel = channelRepository.findById(command.channelId).orElseThrow {
            BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        // ONCE, ALWAYS는 채널+플랫폼당 하나만 허용
        if (command.scheduleType == RecordScheduleType.ONCE ||
            command.scheduleType == RecordScheduleType.ALWAYS) {
            val exists = recordScheduleRepository.existsByChannelAndPlatformTypeAndScheduleType(
                channel = channel,
                platformType = command.platformType,
                scheduleType = command.scheduleType
            )
            if (exists) {
                throw BusinessException(
                    "해당 채널과 플랫폼에 이미 ${command.scheduleType} 스케줄이 존재합니다.",
                    HttpStatus.CONFLICT
                )
            }
        }

        val recordSchedule = RecordSchedule(
            channel = channel,
            platformType = command.platformType,
            scheduleType = command.scheduleType,
            value = command.value,
            recordQuality = command.recordQuality,
            priority = command.priority
        )
        val saved = recordScheduleRepository.save(recordSchedule)

        return RecordScheduleAdminCreateResult.from(
            recordSchedule = saved,
            channelProfileUrl = urlService.channelProfileUrl(channel.uuid)
        )
    }

}
