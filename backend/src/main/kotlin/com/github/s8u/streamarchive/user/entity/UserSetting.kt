package com.github.s8u.streamarchive.user.entity

import com.github.s8u.streamarchive.global.entity.base.BaseEntity
import com.github.s8u.streamarchive.user.enums.UserSettingKey
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Comment
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

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

    // DB ENUM 타입이면 키를 추가해도 ddl-auto가 값 목록을 바꾸지 않아 새 키를 저장하지 못한다.
    // 그래서 문자열 컬럼으로 만든다.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
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
