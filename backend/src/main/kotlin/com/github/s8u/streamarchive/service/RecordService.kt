package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.repository.RecordRepository
import org.springframework.stereotype.Service

@Service
class RecordService(
    private val recordRepository: RecordRepository
) {

}