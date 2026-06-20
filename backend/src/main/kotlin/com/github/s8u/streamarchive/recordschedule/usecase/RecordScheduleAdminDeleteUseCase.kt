package com.github.s8u.streamarchive.recordschedule.usecase

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.recordschedule.repository.RecordScheduleRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 녹화 스케줄 삭제 (관리자)
 */
@Service
class RecordScheduleAdminDeleteUseCase(
    private val recordScheduleRepository: RecordScheduleRepository
) {

    @Transactional
    fun delete(id: Long) {
        val recordSchedule = recordScheduleRepository.findById(id).orElseThrow {
            BusinessException("녹화 스케줄을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        recordSchedule.softDelete(userId = null, ip = null)
    }

}
