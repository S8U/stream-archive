package com.github.s8u.streamarchive.video.repository

import com.github.s8u.streamarchive.video.entity.VideoArchiveHistory
import org.springframework.data.jpa.repository.JpaRepository

interface VideoArchiveHistoryRepository : JpaRepository<VideoArchiveHistory, Long>
