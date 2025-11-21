package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.dto.AdminRecordScheduleSearchRequest
import com.github.s8u.streamarchive.entity.RecordSchedule
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RecordScheduleRepositoryCustom {
    fun searchForAdmin(request: AdminRecordScheduleSearchRequest, pageable: Pageable): Page<RecordSchedule>
}
