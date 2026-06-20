package com.github.s8u.streamarchive.recordschedule.usecase

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.recordschedule.repository.RecordScheduleRepository
import com.github.s8u.streamarchive.recordschedule.usecase.dto.result.RecordScheduleAdminGetResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 녹화 스케줄 단건 조회 (관리자)
 */
@Service
class RecordScheduleAdminGetUseCase(
    private val recordScheduleRepository: RecordScheduleRepository,
    private val urlService: UrlService
) {

    @Transactional(readOnly = true)
    fun get(id: Long): RecordScheduleAdminGetResult {
        val recordSchedule = recordScheduleRepository.findById(id).orElseThrow {
            BusinessException("녹화 스케줄을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return RecordScheduleAdminGetResult.from(
            recordSchedule = recordSchedule,
            channelProfileUrl = urlService.channelProfileUrl(recordSchedule.channel.uuid)
        )
    }

}
