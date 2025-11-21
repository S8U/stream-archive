package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.ChannelPlatform
import com.github.s8u.streamarchive.enums.PlatformType
import org.springframework.data.jpa.repository.JpaRepository

interface ChannelPlatformRepository : JpaRepository<ChannelPlatform, Long>, ChannelPlatformRepositoryCustom {
    fun findByChannelId(channelId: Long): List<ChannelPlatform>
    fun findByChannelIdAndPlatformType(channelId: Long, platformType: PlatformType): ChannelPlatform?
    fun findByIsSyncProfile(isSyncProfile: Boolean): List<ChannelPlatform>
}