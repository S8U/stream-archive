package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.Video
import org.springframework.data.jpa.repository.JpaRepository

interface VideoRepository : JpaRepository<Video, Long>, VideoRepositoryCustom