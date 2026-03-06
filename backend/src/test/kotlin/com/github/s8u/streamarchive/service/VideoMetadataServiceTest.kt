package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.properties.StorageProperties
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class VideoMetadataServiceTest {

    @TempDir
    lateinit var tempDir: Path

    private fun createService(): VideoMetadataService {
        val storageProperties = StorageProperties().apply {
            basePath = tempDir.toString()
        }
        return VideoMetadataService(storageProperties)
    }

    @Nested
    @DisplayName("calculateFileSize")
    inner class CalculateFileSize {

        @Test
        @DisplayName("비디오 디렉토리의 전체 파일 크기를 계산한다")
        fun calculateTotalSize() {
            val service = createService()
            val videoDir = tempDir.resolve("videos/1")
            Files.createDirectories(videoDir)

            // 파일 생성
            Files.write(videoDir.resolve("segment_0.ts"), ByteArray(1000))
            Files.write(videoDir.resolve("segment_1.ts"), ByteArray(2000))
            Files.write(videoDir.resolve("playlist.m3u8"), ByteArray(100))

            val size = service.calculateFileSize(1L)

            assertEquals(3100L, size)
        }

        @Test
        @DisplayName("존재하지 않는 디렉토리는 0을 반환한다")
        fun nonExistentDirectory() {
            val service = createService()

            val size = service.calculateFileSize(999L)

            assertEquals(0L, size)
        }

        @Test
        @DisplayName("빈 디렉토리는 0을 반환한다")
        fun emptyDirectory() {
            val service = createService()
            Files.createDirectories(tempDir.resolve("videos/2"))

            val size = service.calculateFileSize(2L)

            assertEquals(0L, size)
        }
    }

    @Nested
    @DisplayName("calculateDuration")
    inner class CalculateDuration {

        @Test
        @DisplayName("M3U8 파일에서 총 재생 시간을 계산한다")
        fun calculateFromM3u8() {
            val service = createService()
            val videoDir = tempDir.resolve("videos/1")
            Files.createDirectories(videoDir)

            val playlistContent = """
                #EXTM3U
                #EXT-X-VERSION:3
                #EXT-X-TARGETDURATION:4
                #EXTINF:3.000,
                segment_0.ts
                #EXTINF:3.000,
                segment_1.ts
                #EXTINF:3.000,
                segment_2.ts
                #EXTINF:2.500,
                segment_3.ts
                #EXT-X-ENDLIST
            """.trimIndent()

            Files.writeString(videoDir.resolve("playlist.m3u8"), playlistContent)

            val duration = service.calculateDuration(1L)

            assertEquals(11, duration) // 3 + 3 + 3 + 2.5 = 11.5 -> 11 (toInt)
        }

        @Test
        @DisplayName("존재하지 않는 플레이리스트 파일은 0을 반환한다")
        fun nonExistentPlaylist() {
            val service = createService()

            val duration = service.calculateDuration(999L)

            assertEquals(0, duration)
        }

        @Test
        @DisplayName("EXTINF가 없는 플레이리스트는 0을 반환한다")
        fun emptyPlaylist() {
            val service = createService()
            val videoDir = tempDir.resolve("videos/3")
            Files.createDirectories(videoDir)

            Files.writeString(videoDir.resolve("playlist.m3u8"), "#EXTM3U\n#EXT-X-ENDLIST")

            val duration = service.calculateDuration(3L)

            assertEquals(0, duration)
        }
    }
}
