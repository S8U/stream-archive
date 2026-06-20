package com.github.s8u.streamarchive.recordschedule.repository

import com.github.s8u.streamarchive.recordschedule.entity.RecordSchedule
import com.github.s8u.streamarchive.recordschedule.usecase.dto.command.RecordScheduleAdminSearchCommand
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RecordScheduleRepositoryCustom {

    fun searchForAdmin(command: RecordScheduleAdminSearchCommand, pageable: Pageable): Page<RecordSchedule>

}
