package com.github.s8u.streamarchive.record.usecase

import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.record.repository.RecordRepository
import com.github.s8u.streamarchive.record.usecase.dto.command.RecordAdminSearchCommand
import com.github.s8u.streamarchive.record.usecase.dto.result.RecordAdminSearchResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 녹화 기록 목록 조회 (관리자)
 */
@Service
class RecordAdminSearchUseCase(
    private val recordRepository: RecordRepository,
    private val urlService: UrlService
) {

    @Transactional(readOnly = true)
    fun search(command: RecordAdminSearchCommand, pageable: Pageable): Page<RecordAdminSearchResult> {
        return recordRepository.searchForAdmin(command, pageable)
            .map { record ->
                RecordAdminSearchResult.from(
                    record = record,
                    channelProfileUrl = urlService.channelProfileUrl(record.channel?.uuid!!),
                    videoThumbnailUrl = urlService.videoThumbnailUrl(record.video?.uuid!!)
                )
            }
    }

}
