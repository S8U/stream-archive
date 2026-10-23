package com.github.s8u.streamarchive.global.converter

import jakarta.persistence.AttributeConverter

/**
 * enum 이름 변환기
 *
 * enum을 이름 그대로 문자열 컬럼에 저장한다.
 * 엔티티의 enum 필드는 모두 이 변환기를 상속한 변환기로 저장한다.
 */
// @Enumerated로 매핑하면 Hibernate가 지금 enum 값 목록으로 DB ENUM 타입이나 CHECK 제약을 만든다.
// ddl-auto는 이 목록을 고치지 않아서, enum에 값을 더해도 새 값을 저장하지 못한다.
abstract class EnumNameConverter<E : Enum<E>>(
    private val enumClass: Class<E>
) : AttributeConverter<E, String> {

    override fun convertToDatabaseColumn(attribute: E?): String? {
        return attribute?.name
    }

    override fun convertToEntityAttribute(dbData: String?): E? {
        return dbData?.let { java.lang.Enum.valueOf(enumClass, it) }
    }

}
