package com.github.s8u.streamarchive.user.repository

import com.github.s8u.streamarchive.user.entity.UserSetting
import org.springframework.data.jpa.repository.JpaRepository

interface UserSettingRepository : JpaRepository<UserSetting, Long> {

    fun findAllByUserId(userId: Long): List<UserSetting>

}
