package com.github.s8u.streamarchive.channelplatform.repository

import com.github.s8u.streamarchive.channelplatform.entity.ChannelPlatform
import com.github.s8u.streamarchive.platform.enums.PlatformType
import org.springframework.data.jpa.repository.JpaRepository

interface ChannelPlatformRepository : JpaRepository<ChannelPlatform, Long>, ChannelPlatformRepositoryCustom {

    fun findByChannelId(channelId: Long): List<ChannelPlatform>
    fun findByChannelIdAndPlatformType(channelId: Long, platformType: PlatformType): ChannelPlatform?
    fun findByIsSyncProfile(isSyncProfile: Boolean): List<ChannelPlatform>
    fun existsByPlatformTypeAndPlatformChannelId(platformType: PlatformType, platformChannelId: String): Boolean

}
