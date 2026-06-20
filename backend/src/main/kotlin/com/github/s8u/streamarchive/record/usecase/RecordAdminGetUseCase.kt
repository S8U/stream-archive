package com.github.s8u.streamarchive.record.usecase

import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.record.repository.RecordRepository
import com.github.s8u.streamarchive.record.usecase.dto.result.RecordAdminGetResult
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 녹화 기록 단건 조회 (관리자)
 */
@Service
class RecordAdminGetUseCase(
    private val recordRepository: RecordRepository,
    private val urlService: UrlService
) {

    @Transactional(readOnly = true)
    fun get(id: Long): RecordAdminGetResult {
        val record = recordRepository.findById(id).orElseThrow {
            BusinessException("녹화를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return RecordAdminGetResult.from(
            record = record,
            channelProfileUrl = urlService.channelProfileUrl(record.channel?.uuid!!),
            videoThumbnailUrl = urlService.videoThumbnailUrl(record.video?.uuid!!)
        )
    }

}
