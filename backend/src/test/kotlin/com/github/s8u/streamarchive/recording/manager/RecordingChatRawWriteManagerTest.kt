package com.github.s8u.streamarchive.recording.manager

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.s8u.streamarchive.global.properties.StorageProperties
import com.github.s8u.streamarchive.platform.chat.dto.PlatformChatRawMessageDto
import com.github.s8u.streamarchive.platform.enums.PlatformChatRawFormat
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class RecordingChatRawWriteManagerTest {

    @TempDir
    lateinit var tempDir: Path

    private val objectMapper = jacksonObjectMapper()

    private lateinit var storageProperties: StorageProperties
    private lateinit var recordingChatRawWriteManager: RecordingChatRawWriteManager

    @BeforeEach
    fun setUp() {
        storageProperties = StorageProperties().apply { basePath = tempDir.toString() }
        recordingChatRawWriteManager = RecordingChatRawWriteManager(storageProperties, objectMapper)
    }

    @Nested
    inner class Write {

        @Test
        fun `원본 메시지를 한 줄씩 JSON으로 이어 쓴다`() {
            recordingChatRawWriteManager.open(RECORD_ID, VIDEO_ID)

            recordingChatRawWriteManager.write(RECORD_ID, rawMessage(payload = "{\"cmd\":93101}", offsetMillis = 1000))
            recordingChatRawWriteManager.write(RECORD_ID, rawMessage(payload = "줄바꿈\n포함", offsetMillis = 2000))
            recordingChatRawWriteManager.close(RECORD_ID)

            val lines = Files.readAllLines(storageProperties.getVideoChatRawPath(VIDEO_ID))
            assertEquals(2, lines.size)

            val first = objectMapper.readTree(lines[0])
            assertEquals(1000, first.path("offsetMillis").asLong())
            assertEquals("2026-10-14T20:00:01", first.path("receivedAt").asText())
            assertEquals("TEXT", first.path("format").asText())
            assertEquals("{\"cmd\":93101}", first.path("payload").asText())

            val second = objectMapper.readTree(lines[1])
            assertEquals("줄바꿈\n포함", second.path("payload").asText())
        }

        @Test
        fun `닫기 전에도 쓴 줄을 파일에서 읽을 수 있다`() {
            recordingChatRawWriteManager.open(RECORD_ID, VIDEO_ID)

            recordingChatRawWriteManager.write(RECORD_ID, rawMessage(payload = "{}", offsetMillis = 1000))

            assertEquals(1, Files.readAllLines(storageProperties.getVideoChatRawPath(VIDEO_ID)).size)

            recordingChatRawWriteManager.close(RECORD_ID)
        }

        @Test
        fun `기록을 시작하지 않은 녹화의 메시지는 버린다`() {
            recordingChatRawWriteManager.write(RECORD_ID, rawMessage(payload = "{}", offsetMillis = 1000))

            assertFalse(Files.exists(storageProperties.getVideoChatRawPath(VIDEO_ID)))
        }

        @Test
        fun `기록을 끝낸 뒤 늦게 도착한 메시지는 버린다`() {
            recordingChatRawWriteManager.open(RECORD_ID, VIDEO_ID)
            recordingChatRawWriteManager.write(RECORD_ID, rawMessage(payload = "{}", offsetMillis = 1000))
            recordingChatRawWriteManager.close(RECORD_ID)

            recordingChatRawWriteManager.write(RECORD_ID, rawMessage(payload = "{}", offsetMillis = 2000))

            assertEquals(1, Files.readAllLines(storageProperties.getVideoChatRawPath(VIDEO_ID)).size)
        }

        @Test
        fun `받은 메시지가 없으면 파일을 만들지 않는다`() {
            recordingChatRawWriteManager.open(RECORD_ID, VIDEO_ID)
            recordingChatRawWriteManager.close(RECORD_ID)

            assertFalse(Files.exists(storageProperties.getVideoChatRawPath(VIDEO_ID)))
        }

        @Test
        fun `다시 열면 기존 파일에 이어 쓴다`() {
            recordingChatRawWriteManager.open(RECORD_ID, VIDEO_ID)
            recordingChatRawWriteManager.write(RECORD_ID, rawMessage(payload = "{}", offsetMillis = 1000))
            recordingChatRawWriteManager.close(RECORD_ID)

            recordingChatRawWriteManager.open(RECORD_ID, VIDEO_ID)
            recordingChatRawWriteManager.write(RECORD_ID, rawMessage(payload = "{}", offsetMillis = 2000))
            recordingChatRawWriteManager.close(RECORD_ID)

            assertEquals(2, Files.readAllLines(storageProperties.getVideoChatRawPath(VIDEO_ID)).size)
        }

    }

    private fun rawMessage(
        payload: String,
        offsetMillis: Long
    ): PlatformChatRawMessageDto {
        return PlatformChatRawMessageDto(
            format = PlatformChatRawFormat.TEXT,
            payload = payload,
            offsetMillis = offsetMillis,
            receivedAt = RECORD_STARTED_AT.plusNanos(offsetMillis * 1_000_000)
        )
    }

    companion object {
        private const val RECORD_ID = 1L
        private const val VIDEO_ID = 10L
        private val RECORD_STARTED_AT: LocalDateTime = LocalDateTime.of(2026, 10, 14, 20, 0, 0)
    }

}
