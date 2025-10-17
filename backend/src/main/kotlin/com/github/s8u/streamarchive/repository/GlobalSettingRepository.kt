package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.GlobalSetting
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GlobalSettingRepository : JpaRepository<GlobalSetting, Long> {

    fun findBySettingKey(settingKey: String): GlobalSetting?

    fun existsBySettingKey(settingKey: String): Boolean

}