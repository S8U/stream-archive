package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.ChannelPlatform
import com.github.s8u.streamarchive.enums.PlatformType
import org.springframework.data.jpa.repository.JpaRepository

interface ChannelPlatformRepository : JpaRepository<ChannelPlatform, Long>, ChannelPlatformRepositoryCustom {
    fun findByIsActive(isActive: Boolean): List<ChannelPlatform>
    fun findByChannelIdAndIsActive(channelId: Long, isActive: Boolean): List<ChannelPlatform>
    fun findByChannelIdAndPlatformTypeAndIsActive(
        channelId: Long,
        platformType: PlatformType,
        isActive: Boolean
    ): ChannelPlatform?
    fun findByIsSyncProfileAndIsActive(isSyncProfile: Boolean, isActive: Boolean): List<ChannelPlatform>
}