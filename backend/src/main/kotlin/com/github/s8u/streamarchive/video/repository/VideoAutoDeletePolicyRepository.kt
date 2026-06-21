package com.github.s8u.streamarchive.video.repository

import com.github.s8u.streamarchive.video.entity.VideoAutoDeletePolicy
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying

interface VideoAutoDeletePolicyRepository : JpaRepository<VideoAutoDeletePolicy, Long> {

    fun findByChannelIdIsNull(): VideoAutoDeletePolicy?
    fun findByChannelId(channelId: Long): VideoAutoDeletePolicy?
    fun findAllByChannelIdIsNotNull(): List<VideoAutoDeletePolicy>

    @Modifying
    fun deleteByChannelId(channelId: Long)

}
