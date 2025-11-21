package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.entity.GlobalSetting
import com.github.s8u.streamarchive.enums.GlobalSettingKey
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GlobalSettingService(
    private val globalSettingRepository: com.github.s8u.streamarchive.repository.GlobalSettingRepository
) {
    private val logger = LoggerFactory.getLogger(GlobalSettingService::class.java)

    @Transactional(readOnly = true)
    fun get(key: String): GlobalSetting? {
        return globalSettingRepository.findBySettingKey(key)
    }

    @Transactional(readOnly = true)
    fun getValue(key: String): String? {
        return globalSettingRepository.findBySettingKey(key)?.settingValue
    }

    @Transactional
    fun createOrUpdate(key: String, value: String, description: String? = null): GlobalSetting {
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
    fun exists(key: String): Boolean {
        return globalSettingRepository.existsBySettingKey(key)
    }

    @Transactional
    fun initialize() {
        GlobalSettingKey.values().forEach { settingKey ->
            if (!exists(settingKey.name)) {
                createOrUpdate(
                    settingKey.name,
                    settingKey.defaultValue,
                    settingKey.description
                )
                logger.info("Created default setting: ${settingKey.name} = ${settingKey.defaultValue}")
            }
        }
    }
}