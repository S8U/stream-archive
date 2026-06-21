package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.global.properties.StorageProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/**
 * 동영상 파일 삭제 서비스
 *
 * 동영상 디렉토리를 통째로 삭제한다.
 */
@Service
class VideoFileDeleteService(
    private val storageProperties: StorageProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val MAX_ATTEMPTS = 3
    }

    /**
     * [videoId]의 동영상 파일을 삭제한다.
     */
    fun deleteFiles(videoId: Long) {
        val videoDir = storageProperties.getVideoPath(videoId)
        if (!Files.exists(videoDir)) {
            return
        }

        // 삭제 중 새 세그먼트가 추가되거나 파일 핸들이 열려 있으면 실패할 수 있다.
        // 디렉토리가 사라질 때까지 몇 번 다시 시도한다.
        repeat(MAX_ATTEMPTS) { attempt ->
            try {
                deleteRecursively(videoDir)
                logger.info("VideoFileDeleteService: Video files deleted: videoId={}", videoId)
                return
            } catch (e: IOException) {
                logger.warn(
                    "VideoFileDeleteService: Failed to delete video files, retrying: videoId={} attempt={}",
                    videoId, attempt + 1, e
                )
            }
        }

        logger.error("VideoFileDeleteService: Gave up deleting video files: videoId={}", videoId)
    }

    // 디렉토리 트리를 순회하며 파일과 폴더를 삭제한다 (순회 중 변경 허용)
    private fun deleteRecursively(root: Path) {
        Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {
            override fun visitFile(
                file: Path,
                attrs: BasicFileAttributes
            ): FileVisitResult {
                Files.deleteIfExists(file)
                return FileVisitResult.CONTINUE
            }

            // 항목 속성을 읽기도 전에 파일이 사라지면(예: macOS+NFS에서 본 파일을 지울 때
            // 짝꿍 ._ AppleDouble 파일이 함께 사라짐) 그 항목은 이미 목표를 이뤘으니 넘어간다.
            override fun visitFileFailed(
                file: Path,
                exc: IOException
            ): FileVisitResult {
                if (exc is NoSuchFileException) {
                    return FileVisitResult.CONTINUE
                }
                throw exc
            }

            override fun postVisitDirectory(
                dir: Path,
                exc: IOException?
            ): FileVisitResult {
                Files.deleteIfExists(dir)
                return FileVisitResult.CONTINUE
            }
        })
    }

}
