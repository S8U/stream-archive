package com.github.s8u.streamarchive.global.converter

import jakarta.persistence.Convert
import jakarta.persistence.EntityManagerFactory
import java.lang.reflect.Field
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class EnumNameConverterTest @Autowired constructor(
    private val entityManagerFactory: EntityManagerFactory,
    private val jdbcTemplate: JdbcTemplate
) {

    @Test
    fun `엔티티의 enum 필드는 모두 이 변환기를 상속한 변환기로 저장한다`() {
        val fieldsWithoutConverter = entityManagerFactory.metamodel.entities
            .flatMap { it.attributes }
            .filter { it.javaType.isEnum }
            .map { it.javaMember as Field }
            .filter { !isEnumNameConverted(it) }
            .map { "${it.declaringClass.simpleName}.${it.name}" }
            .distinct()

        assertEquals(emptyList(), fieldsWithoutConverter)
    }

    @Test
    fun `새로 만든 테이블에 값 목록을 가진 컬럼이 없다`() {
        // 테스트 DB(H2)는 엔티티로 테이블을 새로 만든다.
        // @Enumerated면 ENUM 타입 컬럼이, 여기에 VARCHAR 타입을 강제하면 CHECK 제약이 생긴다.
        val enumColumns = jdbcTemplate.queryForList(
            """
            SELECT TABLE_NAME, COLUMN_NAME
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = SCHEMA() AND DATA_TYPE = 'ENUM'
            """
        )
        val checkClauses = jdbcTemplate.queryForList(
            """
            SELECT CHECK_CLAUSE
            FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
            WHERE CONSTRAINT_SCHEMA = SCHEMA()
            """,
            String::class.java
        )
        // @Min 같은 다른 CHECK는 두고, 값 목록 CHECK만 찾는다. H2는 "ROLE" IN('ADMIN', 'USER')처럼 적는다.
        val valueListChecks = checkClauses.filter { it.contains(" IN(") }

        assertEquals(emptyList(), enumColumns)
        assertEquals(emptyList(), valueListChecks)
    }

    private fun isEnumNameConverted(field: Field): Boolean {
        val converter = field.getAnnotation(Convert::class.java)?.converter?.java
            ?: return false

        return EnumNameConverter::class.java.isAssignableFrom(converter)
    }

}
