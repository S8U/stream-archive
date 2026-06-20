package com.github.s8u.streamarchive.recordschedule.usecase

import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.recordschedule.repository.RecordScheduleRepository
import com.github.s8u.streamarchive.recordschedule.usecase.dto.command.RecordScheduleAdminSearchCommand
import com.github.s8u.streamarchive.recordschedule.usecase.dto.result.RecordScheduleAdminSearchResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 녹화 스케줄 목록 조회 (관리자)
 */
@Service
class RecordScheduleAdminSearchUseCase(
    private val recordScheduleRepository: RecordScheduleRepository,
    private val urlService: UrlService
) {

    @Transactional(readOnly = true)
    fun search(command: RecordScheduleAdminSearchCommand, pageable: Pageable): Page<RecordScheduleAdminSearchResult> {
        return recordScheduleRepository.searchForAdmin(command, pageable)
            .map { recordSchedule ->
                RecordScheduleAdminSearchResult.from(
                    recordSchedule = recordSchedule,
                    channelProfileUrl = urlService.channelProfileUrl(recordSchedule.channel.uuid)
                )
            }
    }

}
