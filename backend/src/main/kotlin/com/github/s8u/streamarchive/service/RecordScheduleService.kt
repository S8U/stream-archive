package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.repository.RecordScheduleRepository
import org.springframework.stereotype.Service

@Service
class RecordScheduleService(
    private val recordScheduleRepository: RecordScheduleRepository
) {
}