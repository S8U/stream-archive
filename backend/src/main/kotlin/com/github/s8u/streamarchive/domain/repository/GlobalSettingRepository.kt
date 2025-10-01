package com.github.s8u.streamarchive.domain.repository

import com.github.s8u.streamarchive.domain.entity.GlobalSetting
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GlobalSettingRepository : JpaRepository<GlobalSetting, Long> {
    fun findBySettingKey(settingKey: String): GlobalSetting?
    fun existsBySettingKey(settingKey: String): Boolean
}