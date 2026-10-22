package com.github.s8u.streamarchive.user.converter

import com.github.s8u.streamarchive.user.enums.UserSettingKey
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * 사용자 설정 키 변환기
 *
 * 설정 키를 enum 이름 그대로 문자열 컬럼에 저장한다.
 */
// @Enumerated로 매핑하면 Hibernate가 지금 키 목록으로 DB ENUM 타입이나 CHECK 제약을 만든다.
// ddl-auto는 이 목록을 고치지 않아서, 키를 추가해도 새 키를 저장하지 못한다.
@Converter
class UserSettingKeyConverter : AttributeConverter<UserSettingKey, String> {

    override fun convertToDatabaseColumn(attribute: UserSettingKey?): String? {
        return attribute?.name
    }

    override fun convertToEntityAttribute(dbData: String?): UserSettingKey? {
        return dbData?.let { UserSettingKey.valueOf(it) }
    }

}
