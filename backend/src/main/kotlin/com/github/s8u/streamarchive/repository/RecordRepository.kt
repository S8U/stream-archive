package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.Record
import org.springframework.data.jpa.repository.JpaRepository

interface RecordRepository : JpaRepository<Record, Long> {
}