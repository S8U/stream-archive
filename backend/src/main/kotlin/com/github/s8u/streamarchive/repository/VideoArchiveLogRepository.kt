package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.VideoArchiveLog
import org.springframework.data.jpa.repository.JpaRepository

interface VideoArchiveLogRepository : JpaRepository<VideoArchiveLog, Long>
