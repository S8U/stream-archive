package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.UserVideoWatchHistory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface UserVideoWatchHistoryRepository : JpaRepository<UserVideoWatchHistory, Long> {
    fun findByUserIdAndVideoId(userId: Long, videoId: Long): UserVideoWatchHistory?
    fun findByUserIdOrderByWatchedAtDesc(userId: Long, pageable: Pageable): Page<UserVideoWatchHistory>

    @Modifying
    @Query("DELETE FROM UserVideoWatchHistory h WHERE h.userId = :userId AND h.videoId = :videoId")
    fun deleteByUserIdAndVideoId(userId: Long, videoId: Long)

    @Modifying
    @Query("DELETE FROM UserVideoWatchHistory h WHERE h.userId = :userId")
    fun deleteAllByUserId(userId: Long)
}
