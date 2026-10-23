package com.github.s8u.streamarchive.global.config

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SwaggerConfigTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper
) {

    @Nested
    inner class ModelResolver {

        @Test
        fun `안쪽 클래스는 바깥 클래스 이름을 붙여 응답마다 스키마를 따로 만든다`() {
            val schemas = getSchemas()

            assertFalse(schemas.has("ChannelInfo"))
            assertEquals(
                listOf("contentPrivacy", "name", "profileUrl", "uuid"),
                getPropertyNames(schemas, "VideoGetResponseChannelInfo")
            )
            assertEquals(
                listOf("id", "name", "profileUrl"),
                getPropertyNames(schemas, "ChannelPlatformAdminGetResponseChannelInfo")
            )
        }

        @Test
        fun `바깥 클래스는 클래스 이름을 그대로 쓴다`() {
            val schemas = getSchemas()

            assertTrue(schemas.has("VideoGetResponse"))
        }
    }

    private fun getSchemas(): JsonNode {
        val body = mockMvc.get("/v3/api-docs").andReturn().response.contentAsString

        return objectMapper.readTree(body).path("components").path("schemas")
    }

    private fun getPropertyNames(schemas: JsonNode, schemaName: String): List<String> {
        return schemas
            .path(schemaName)
            .path("properties")
            .fieldNames()
            .asSequence()
            .sorted()
            .toList()
    }

}
