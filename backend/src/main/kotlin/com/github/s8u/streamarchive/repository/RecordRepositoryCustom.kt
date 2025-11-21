package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.dto.AdminRecordSearchRequest
import com.github.s8u.streamarchive.entity.Record
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RecordRepositoryCustom {
    fun searchForAdmin(request: AdminRecordSearchRequest, pageable: Pageable): Page<Record>
}
