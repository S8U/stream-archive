package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.AdminRecordScheduleCreateRequest
import com.github.s8u.streamarchive.dto.AdminRecordScheduleResponse
import com.github.s8u.streamarchive.dto.AdminRecordScheduleSearchRequest
import com.github.s8u.streamarchive.dto.AdminRecordScheduleUpdateRequest
import com.github.s8u.streamarchive.entity.RecordSchedule
import com.github.s8u.streamarchive.enums.RecordScheduleType
import com.github.s8u.streamarchive.exception.BusinessException
import com.github.s8u.streamarchive.repository.RecordScheduleRepository
import com.github.s8u.streamarchive.util.UrlBuilder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class RecordScheduleService(
    private val recordScheduleRepository: RecordScheduleRepository,
    private val urlBuilder: UrlBuilder
) {
    @Transactional(readOnly = true)
    fun searchForAdmin(request: AdminRecordScheduleSearchRequest, pageable: Pageable): Page<AdminRecordScheduleResponse> {
        return recordScheduleRepository.searchForAdmin(request, pageable)
            .map { recordSchedule ->
                val channelProfileUrl = urlBuilder.channelProfileUrl(recordSchedule.channel?.uuid!!)
                AdminRecordScheduleResponse.from(
                    recordSchedule = recordSchedule,
                    channelProfileUrl = channelProfileUrl
                )
            }
    }

    @Transactional(readOnly = true)
    fun getForAdmin(id: Long): AdminRecordScheduleResponse {
        val recordSchedule = recordScheduleRepository.findById(id).orElseThrow {
            BusinessException("녹화 스케줄을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val channelProfileUrl = urlBuilder.channelProfileUrl(recordSchedule.channel?.uuid!!)
        return AdminRecordScheduleResponse.from(
            recordSchedule = recordSchedule,
            channelProfileUrl = channelProfileUrl
        )
    }

    @Transactional
    fun createForAdmin(request: AdminRecordScheduleCreateRequest): AdminRecordScheduleResponse {
        // ONCE, ALWAYS는 채널+플랫폼당 하나만 허용
        if (request.scheduleType == RecordScheduleType.ONCE ||
            request.scheduleType == RecordScheduleType.ALWAYS) {
            val exists = recordScheduleRepository.existsByChannelIdAndPlatformTypeAndScheduleType(
                channelId = request.channelId,
                platformType = request.platformType,
                scheduleType = request.scheduleType
            )
            if (exists) {
                throw BusinessException(
                    "해당 채널과 플랫폼에 이미 ${request.scheduleType} 스케줄이 존재합니다.",
                    HttpStatus.CONFLICT
                )
            }
        }

        val recordSchedule = RecordSchedule(
            channelId = request.channelId,
            platformType = request.platformType,
            scheduleType = request.scheduleType,
            value = request.value,
            recordQuality = request.recordQuality,
            priority = request.priority
        )
        val saved = recordScheduleRepository.save(recordSchedule)

        // 저장 후 다시 조회하여 Channel 정보 로드
        return getForAdmin(saved.id!!)
    }

    @Transactional
    fun updateForAdmin(id: Long, request: AdminRecordScheduleUpdateRequest): AdminRecordScheduleResponse {
        val recordSchedule = recordScheduleRepository.findById(id).orElseThrow {
            BusinessException("녹화 스케줄을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        // platformType 또는 scheduleType 변경 시 중복 체크
        val newPlatformType = request.platformType ?: recordSchedule.platformType
        val newScheduleType = request.scheduleType ?: recordSchedule.scheduleType

        // ONCE, ALWAYS는 채널+플랫폼당 하나만 허용
        if (newScheduleType == RecordScheduleType.ONCE ||
            newScheduleType == RecordScheduleType.ALWAYS) {
            // platformType이나 scheduleType이 변경되는 경우만 체크
            if (request.platformType != null || request.scheduleType != null) {
                val exists = recordScheduleRepository.existsByChannelIdAndPlatformTypeAndScheduleType(
                    channelId = recordSchedule.channelId,
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

        request.platformType?.let { recordSchedule.platformType = it }
        request.scheduleType?.let { recordSchedule.scheduleType = it }
        request.value?.let { recordSchedule.value = it }
        request.recordQuality?.let { recordSchedule.recordQuality = it }
        request.priority?.let { recordSchedule.priority = it }

        // 업데이트 후 다시 조회하여 최신 정보 로드
        return getForAdmin(recordSchedule.id!!)
    }

    @Transactional
    fun delete(id: Long) {
        val recordSchedule = recordScheduleRepository.findById(id).orElseThrow {
            BusinessException("녹화 스케줄을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        recordSchedule.isActive = false
        recordSchedule.deletedAt = LocalDateTime.now()
    }

    @Transactional
    fun deleteAllByChannelId(channelId: Long) {
        val recordSchedules = recordScheduleRepository.findByChannelId(channelId)

        recordSchedules.forEach { recordSchedule ->
            recordSchedule.isActive = false
            recordSchedule.deletedAt = LocalDateTime.now()
        }
    }

}