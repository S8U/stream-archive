package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.entity.ChannelSetting
import com.github.s8u.streamarchive.enums.ChannelSettingKey
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChannelSettingService(
    private val channelSettingRepository: com.github.s8u.streamarchive.repository.ChannelSettingRepository,
    private val globalSettingService: GlobalSettingService
) {
    private val logger = LoggerFactory.getLogger(ChannelSettingService::class.java)

    @Transactional(readOnly = true)
    fun getSetting(channelId: Long, key: String): ChannelSetting? {
        return channelSettingRepository.findByChannelIdAndSettingKey(channelId, key)
    }

    @Transactional(readOnly = true)
    fun getSettingValue(channelId: Long, key: String): String? {
        return channelSettingRepository.findByChannelIdAndSettingKey(channelId, key)?.settingValue
            ?: globalSettingService.getSettingValue(key)
    }

    @Transactional(readOnly = true)
    fun getAllSettings(channelId: Long): List<ChannelSetting> {
        return channelSettingRepository.findAllByChannelId(channelId)
    }

    @Transactional
    fun createOrUpdateSetting(channelId: Long, key: String, value: String): ChannelSetting {
        val setting = channelSettingRepository.findByChannelIdAndSettingKey(channelId, key)
            ?: ChannelSetting(
                channelId = channelId,
                settingKey = key,
                settingValue = value
            )

        setting.settingValue = value

        return channelSettingRepository.save(setting)
    }

    @Transactional
    fun deleteSetting(channelId: Long, key: String) {
        channelSettingRepository.deleteByChannelIdAndSettingKey(channelId, key)
    }

    @Transactional(readOnly = true)
    fun existsSetting(channelId: Long, key: String): Boolean {
        return channelSettingRepository.existsByChannelIdAndSettingKey(channelId, key)
    }

    @Transactional
    fun initializeDefaultSettings(channelId: Long) {
        ChannelSettingKey.values().forEach { settingKey ->
            if (settingKey.defaultValue != null && !existsSetting(channelId, settingKey.name)) {
                createOrUpdateSetting(channelId, settingKey.name, settingKey.defaultValue)
                logger.info("Created default channel setting for channel {}: {} = {}",
                    channelId, settingKey.name, settingKey.defaultValue)
            }
        }
    }
}