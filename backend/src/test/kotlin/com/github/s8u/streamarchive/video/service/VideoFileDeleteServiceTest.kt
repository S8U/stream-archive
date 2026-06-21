package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.global.properties.StorageProperties
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertFalse
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class VideoFileDeleteServiceTest {

    @TempDir
    lateinit var tempDir: Path

    @Nested
    inner class DeleteFiles {

        @Test
        fun `동영상 디렉터리와 내부 파일을 모두 삭제한다`() {
            val storageProperties = StorageProperties().apply { basePath = tempDir.toString() }
            val videoFileDeleteService = VideoFileDeleteService(storageProperties)
            val videoDir = storageProperties.getVideoPath(VIDEO_ID)
            val segmentDir = videoDir.resolve("segments")
            Files.createDirectories(segmentDir)
            Files.writeString(videoDir.resolve("playlist.m3u8"), "playlist")
            Files.writeString(segmentDir.resolve("segment-1.ts"), "segment")

            videoFileDeleteService.deleteFiles(VIDEO_ID)

            assertFalse(Files.exists(videoDir))
        }

        @Test
        fun `동영상 디렉터리가 없어도 예외를 던지지 않는다`() {
            val storageProperties = StorageProperties().apply { basePath = tempDir.toString() }
            val videoFileDeleteService = VideoFileDeleteService(storageProperties)

            videoFileDeleteService.deleteFiles(VIDEO_ID)

            assertFalse(Files.exists(storageProperties.getVideoPath(VIDEO_ID)))
        }

    }

    companion object {
        private const val VIDEO_ID = 1L
    }

}
