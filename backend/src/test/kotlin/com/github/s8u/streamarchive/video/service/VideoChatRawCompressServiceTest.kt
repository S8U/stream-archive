package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.global.properties.StorageProperties
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.GZIPInputStream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class VideoChatRawCompressServiceTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var storageProperties: StorageProperties
    private lateinit var videoChatRawCompressService: VideoChatRawCompressService

    @BeforeEach
    fun setUp() {
        storageProperties = StorageProperties().apply { basePath = tempDir.toString() }
        videoChatRawCompressService = VideoChatRawCompressService(storageProperties)
    }

    @Nested
    inner class Compress {

        @Test
        fun `평문 원본을 gzip으로 압축하고 평문을 지운다`() {
            val rawPath = storageProperties.getVideoChatRawPath(VIDEO_ID)
            Files.createDirectories(rawPath.parent)
            Files.writeString(rawPath, RAW_CONTENT)

            videoChatRawCompressService.compress(VIDEO_ID)

            val compressedPath = storageProperties.getVideoChatRawCompressedPath(VIDEO_ID)
            assertFalse(Files.exists(rawPath))
            assertTrue(Files.exists(compressedPath))
            assertFalse(Files.exists(compressedPath.resolveSibling("${compressedPath.fileName}.tmp")))

            val actual = GZIPInputStream(Files.newInputStream(compressedPath)).use {
                it.readAllBytes().toString(Charsets.UTF_8)
            }
            assertEquals(RAW_CONTENT, actual)
        }

        @Test
        fun `평문 원본이 없으면 아무것도 하지 않는다`() {
            videoChatRawCompressService.compress(VIDEO_ID)

            assertFalse(Files.exists(storageProperties.getVideoChatRawCompressedPath(VIDEO_ID)))
        }

    }

    companion object {
        private const val VIDEO_ID = 10L
        private const val RAW_CONTENT = "{\"offsetMillis\":1000,\"payload\":\"채팅\"}\n"
    }

}
