package com.github.s8u.streamarchive.user.repository

import com.github.s8u.streamarchive.user.entity.UserSetting
import com.github.s8u.streamarchive.user.enums.UserSettingKey
import org.springframework.data.jpa.repository.JpaRepository

interface UserSettingRepository : JpaRepository<UserSetting, Long> {

    // UserSettingKey에서 없앤 키의 행이 남아 있으면 enum으로 바꾸지 못해 조회가 실패한다.
    // 그래서 사용자 설정은 전부 읽지 않고, 지금 있는 키(UserSettingKey.entries)만 골라 읽는다.
    fun findAllByUserIdAndSettingKeyIn(
        userId: Long,
        settingKeys: Collection<UserSettingKey>
    ): List<UserSetting>

}
