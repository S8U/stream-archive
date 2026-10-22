package com.github.s8u.streamarchive.user.entity

import com.github.s8u.streamarchive.global.entity.base.BaseEntity
import com.github.s8u.streamarchive.user.converter.UserSettingKeyConverter
import com.github.s8u.streamarchive.user.enums.UserSettingKey
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Comment

@Entity
@Table(
    name = "user_settings",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_user_settings_user_id_setting_key", columnNames = ["userId", "settingKey"])
    ]
)
@Comment("사용자 설정")
class UserSetting(
    @Column(nullable = false)
    @Comment("사용자 ID")
    val userId: Long,

    // 키를 추가해도 저장할 수 있게 @Enumerated 대신 변환기로 문자열 컬럼에 저장한다 (이유는 UserSettingKeyConverter)
    @Convert(converter = UserSettingKeyConverter::class)
    @Column(nullable = false, length = 50)
    @Comment("설정 키 (SHOW_UNLISTED_CHANNELS/SHOW_UNLISTED_VIDEOS)")
    val settingKey: UserSettingKey,

    settingValue: String
) : BaseEntity() {

    @Column(nullable = false, length = 255)
    @Comment("설정 값")
    var settingValue: String = settingValue
        protected set

    /**
     * 설정 값을 바꾼다.
     */
    fun changeValue(settingValue: String) {
        this.settingValue = settingValue
    }

}
