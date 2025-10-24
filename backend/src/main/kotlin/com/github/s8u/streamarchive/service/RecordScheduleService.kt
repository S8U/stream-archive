package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.dto.RecordScheduleCreateRequest
import com.github.s8u.streamarchive.dto.RecordScheduleResponse
import com.github.s8u.streamarchive.dto.RecordScheduleUpdateRequest
import com.github.s8u.streamarchive.entity.RecordSchedule
import com.github.s8u.streamarchive.enums.RecordScheduleType
import com.github.s8u.streamarchive.repository.RecordScheduleRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordScheduleService(
    private val recordScheduleRepository: RecordScheduleRepository
) {
    @Transactional(readOnly = true)
    fun getAll(): List<RecordScheduleResponse> {
        return recordScheduleRepository.findAll()
            .map { RecordScheduleResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): RecordScheduleResponse {
        val recordSchedule = recordScheduleRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("RecordSchedule not found: $id")
        return RecordScheduleResponse.from(recordSchedule)
    }

    @Transactional
    fun create(request: RecordScheduleCreateRequest): RecordScheduleResponse {
        // ONCE, ALWAYS는 채널+플랫폼당 하나만 허용
        if (request.scheduleType == RecordScheduleType.ONCE ||
            request.scheduleType == RecordScheduleType.ALWAYS) {
            val exists = recordScheduleRepository.existsByChannelIdAndPlatformTypeAndScheduleTypeAndIsActive(
                channelId = request.channelId,
                platformType = request.platformType,
                scheduleType = request.scheduleType,
                isActive = true
            )
            if (exists) {
                throw IllegalStateException(
                    "Active ${request.scheduleType} schedule already exists for channel ${request.channelId} and platform ${request.platformType}"
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
        return RecordScheduleResponse.from(saved)
    }

    @Transactional
    fun update(id: Long, request: RecordScheduleUpdateRequest): RecordScheduleResponse {
        val recordSchedule = recordScheduleRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("RecordSchedule not found: $id")

        // platformType 또는 scheduleType 변경 시 중복 체크
        val newPlatformType = request.platformType ?: recordSchedule.platformType
        val newScheduleType = request.scheduleType ?: recordSchedule.scheduleType

        // ONCE, ALWAYS는 채널+플랫폼당 하나만 허용
        if (newScheduleType == RecordScheduleType.ONCE ||
            newScheduleType == RecordScheduleType.ALWAYS) {
            // platformType이나 scheduleType이 변경되는 경우만 체크
            if (request.platformType != null || request.scheduleType != null) {
                val exists = recordScheduleRepository.existsByChannelIdAndPlatformTypeAndScheduleTypeAndIsActive(
                    channelId = recordSchedule.channelId,
                    platformType = newPlatformType,
                    scheduleType = newScheduleType,
                    isActive = true
                )
                // 자기 자신이 아닌 다른 스케줄이 있는지 확인
                if (exists && (recordSchedule.platformType != newPlatformType ||
                              recordSchedule.scheduleType != newScheduleType)) {
                    throw IllegalStateException(
                        "Active $newScheduleType schedule already exists for channel ${recordSchedule.channelId} and platform $newPlatformType"
                    )
                }
            }
        }

        request.platformType?.let { recordSchedule.platformType = it }
        request.scheduleType?.let { recordSchedule.scheduleType = it }
        request.value?.let { recordSchedule.value = it }
        request.recordQuality?.let { recordSchedule.recordQuality = it }
        request.priority?.let { recordSchedule.priority = it }
        request.isActive?.let { recordSchedule.isActive = it }

        return RecordScheduleResponse.from(recordSchedule)
    }

    @Transactional
    fun delete(id: Long) {
        val recordSchedule = recordScheduleRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("RecordSchedule not found: $id")

        recordSchedule.isActive = false
    }
}