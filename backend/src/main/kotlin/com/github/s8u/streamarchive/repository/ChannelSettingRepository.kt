package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.ChannelSetting
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChannelSettingRepository : JpaRepository<ChannelSetting, Long> {

    fun findAllByChannelId(channelId: Long): List<ChannelSetting>

    fun findByChannelIdAndSettingKey(channelId: Long, settingKey: String): ChannelSetting?

    fun existsByChannelIdAndSettingKey(channelId: Long, settingKey: String): Boolean

    fun deleteByChannelIdAndSettingKey(channelId: Long, settingKey: String)

}