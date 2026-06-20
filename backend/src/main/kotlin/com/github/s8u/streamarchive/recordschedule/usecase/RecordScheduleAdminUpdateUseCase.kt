package com.github.s8u.streamarchive.recordschedule.usecase

import com.github.s8u.streamarchive.recordschedule.enums.RecordScheduleType
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.recordschedule.repository.RecordScheduleRepository
import com.github.s8u.streamarchive.recordschedule.usecase.dto.command.RecordScheduleAdminUpdateCommand
import com.github.s8u.streamarchive.recordschedule.usecase.dto.result.RecordScheduleAdminUpdateResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 녹화 스케줄 수정 (관리자)
 */
@Service
class RecordScheduleAdminUpdateUseCase(
    private val recordScheduleRepository: RecordScheduleRepository,
    private val urlService: UrlService
) {

    @Transactional
    fun update(id: Long, command: RecordScheduleAdminUpdateCommand): RecordScheduleAdminUpdateResult {
        val recordSchedule = recordScheduleRepository.findById(id).orElseThrow {
            BusinessException("녹화 스케줄을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        // platformType 또는 scheduleType 변경 시 중복 체크
        val newPlatformType = command.platformType ?: recordSchedule.platformType
        val newScheduleType = command.scheduleType ?: recordSchedule.scheduleType

        // ONCE, ALWAYS는 채널+플랫폼당 하나만 허용
        if (newScheduleType == RecordScheduleType.ONCE ||
            newScheduleType == RecordScheduleType.ALWAYS) {
            // platformType이나 scheduleType이 변경되는 경우만 체크
            if (command.platformType != null || command.scheduleType != null) {
                val exists = recordScheduleRepository.existsByChannelAndPlatformTypeAndScheduleType(
                    channel = recordSchedule.channel,
                    platformType = newPlatformType,
                    scheduleType = newScheduleType
                )
                // 자기 자신이 아닌 다른 스케줄이 있는지 확인
                if (exists && (recordSchedule.platformType != newPlatformType ||
                              recordSchedule.scheduleType != newScheduleType)) {
                    throw BusinessException(
                        "해당 채널과 플랫폼에 이미 $newScheduleType 스케줄이 존재합니다.",
                        HttpStatus.CONFLICT
                    )
                }
            }
        }

        recordSchedule.update(
            platformType = command.platformType,
            scheduleType = command.scheduleType,
            value = command.value,
            recordQuality = command.recordQuality,
            priority = command.priority,
            autoArchive = command.autoArchive
        )

        return RecordScheduleAdminUpdateResult.from(
            recordSchedule = recordSchedule,
            channelProfileUrl = urlService.channelProfileUrl(recordSchedule.channel.uuid)
        )
    }

}
