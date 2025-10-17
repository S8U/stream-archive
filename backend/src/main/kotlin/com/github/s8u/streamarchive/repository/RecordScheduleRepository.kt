package com.github.s8u.streamarchive.repository

import org.springframework.data.jpa.repository.JpaRepository

interface RecordScheduleRepository : JpaRepository<com.github.s8u.streamarchive.entity.RecordSchedule, Long> {
}