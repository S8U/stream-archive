package com.github.s8u.streamarchive

import jakarta.persistence.EntityManagerFactory
import kotlin.test.assertEquals
import org.hibernate.SessionFactory
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.jdbc.core.ConnectionCallback
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.MariaDBContainer

/**
 * Flyway 마이그레이션 테스트
 *
 * 빈 MariaDB에 마이그레이션을 모두 실행한 스키마가 엔티티로 만든 스키마와 같은지 확인한다.
 * 엔티티를 고치고 마이그레이션을 빠뜨리면 이 테스트가 실패한다.
 */
@SpringBootTest(
    properties = [
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect"
    ]
)
@ActiveProfiles("test")
class FlywayMigrationTest @Autowired constructor(
    private val entityManagerFactory: EntityManagerFactory,
    private val jdbcTemplate: JdbcTemplate
) {

    @Test
    fun `마이그레이션으로 만든 스키마가 엔티티로 만든 스키마와 같다`() {
        // 기동할 때 Flyway가 빈 DB에 마이그레이션을 모두 실행했다
        val migratedSchema = readSchema()

        // 마이그레이션이 만든 테이블을 모두 지우고, Hibernate가 엔티티로 테이블을 다시 만든다
        dropTables()
        entityManagerFactory.unwrap(SessionFactory::class.java).schemaManager.exportMappedObjects(false)
        val entitySchema = readSchema()

        assertEquals(emptyList(), entitySchema - migratedSchema.toSet(), "마이그레이션에 없는 엔티티 스키마")
        assertEquals(emptyList(), migratedSchema - entitySchema.toSet(), "엔티티에 없는 마이그레이션 스키마")
    }

    // 스키마를 비교할 수 있게 한 줄씩 읽는다
    // 컬럼 순서는 비교하지 않는다. 나중에 마이그레이션으로 더한 컬럼은 테이블 끝에 붙어서 엔티티와 순서가 다르다.
    private fun readSchema(): List<String> {
        val queries = listOf(
            """
            SELECT 'table', TABLE_NAME, ENGINE, TABLE_COLLATION, TABLE_COMMENT
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME != '$FLYWAY_HISTORY_TABLE'
            """,
            """
            SELECT 'column', TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT,
                EXTRA, COLLATION_NAME, COLUMN_COMMENT
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME != '$FLYWAY_HISTORY_TABLE'
            """,
            """
            SELECT 'index', TABLE_NAME, INDEX_NAME, NON_UNIQUE, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX)
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME != '$FLYWAY_HISTORY_TABLE'
            GROUP BY TABLE_NAME, INDEX_NAME, NON_UNIQUE
            """,
            """
            SELECT 'foreign key', TABLE_NAME, CONSTRAINT_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
            FROM information_schema.KEY_COLUMN_USAGE
            WHERE TABLE_SCHEMA = DATABASE() AND REFERENCED_TABLE_NAME IS NOT NULL
            """,
            """
            SELECT 'check', TABLE_NAME, CONSTRAINT_NAME, CHECK_CLAUSE
            FROM information_schema.CHECK_CONSTRAINTS
            WHERE CONSTRAINT_SCHEMA = DATABASE()
            """
        )

        return queries
            .flatMap { jdbcTemplate.queryForList(it) }
            .map { row -> row.values.joinToString(" | ") }
            .sorted()
    }

    private fun dropTables() {
        val tableNames = jdbcTemplate.queryForList(
            """
            SELECT TABLE_NAME
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME != '$FLYWAY_HISTORY_TABLE'
            """,
            String::class.java
        )

        // 외래 키 순서를 따지지 않고 지우려고 외래 키 검사를 끈다. 이 설정은 연결마다 따로라서 한 연결에서 지운다.
        jdbcTemplate.execute(ConnectionCallback { connection ->
            connection.createStatement().use { statement ->
                statement.execute("SET FOREIGN_KEY_CHECKS = 0")
                tableNames.forEach { statement.execute("DROP TABLE $it") }
                statement.execute("SET FOREIGN_KEY_CHECKS = 1")
            }
        })
    }

    companion object {

        private const val FLYWAY_HISTORY_TABLE = "flyway_schema_history"

        // 운영 DB와 같은 버전이다
        // 컨테이너를 테스트 클래스가 끝나도 멈추지 않는다. 스프링이 이 테스트의 컨텍스트를 캐시해 두고 다시 쓰기 때문이다.
        @ServiceConnection
        @JvmStatic
        val mariaDb: MariaDBContainer<*> = MariaDBContainer("mariadb:11.8").apply { start() }

    }

}
