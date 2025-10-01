package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.domain.entity.GlobalSetting
import com.github.s8u.streamarchive.domain.enums.GlobalSettingKey
import com.github.s8u.streamarchive.domain.repository.GlobalSettingRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GlobalSettingService(
    private val globalSettingRepository: GlobalSettingRepository
) {
    private val logger = LoggerFactory.getLogger(GlobalSettingService::class.java)

    @Transactional(readOnly = true)
    fun getSetting(key: String): GlobalSetting? {
        return globalSettingRepository.findBySettingKey(key)
    }

    @Transactional(readOnly = true)
    fun getSettingValue(key: String): String? {
        return globalSettingRepository.findBySettingKey(key)?.settingValue
    }

    @Transactional
    fun createOrUpdateSetting(key: String, value: String, description: String? = null): GlobalSetting {
        val setting = globalSettingRepository.findBySettingKey(key)
            ?: GlobalSetting(
                settingKey = key,
                settingValue = value,
                description = description
            )

        setting.settingValue = value
        setting.description = description

        return globalSettingRepository.save(setting)
    }

    @Transactional(readOnly = true)
    fun existsSetting(key: String): Boolean {
        return globalSettingRepository.existsBySettingKey(key)
    }

    @Transactional
    fun initializeDefaultSettings() {
        GlobalSettingKey.values().forEach { settingKey ->
            if (!existsSetting(settingKey.key)) {
                createOrUpdateSetting(
                    settingKey.key,
                    settingKey.defaultValue,
                    settingKey.description
                )
                logger.info("Created default setting: ${settingKey.key} = ${settingKey.defaultValue}")
            }
        }
    }
}