package com.github.s8u.streamarchive.record.repository

import com.github.s8u.streamarchive.record.entity.Record
import com.github.s8u.streamarchive.record.usecase.dto.command.RecordAdminSearchCommand
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RecordRepositoryCustom {

    fun searchForAdmin(command: RecordAdminSearchCommand, pageable: Pageable): Page<Record>

}
