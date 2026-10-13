package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.global.properties.StorageProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.GZIPOutputStream

/**
 * 동영상 채팅 원본 압축 서비스
 *
 * 녹화 중 이어 쓴 채팅 원본 파일을 gzip으로 압축하고 평문 파일을 지운다.
 */
@Service
class VideoChatRawCompressService(
    private val storageProperties: StorageProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * [videoId]의 채팅 원본 파일을 압축한다.
     *
     * 평문 파일이 없으면 아무것도 하지 않는다.
     */
    fun compress(videoId: Long) {
        val rawPath = storageProperties.getVideoChatRawPath(videoId)
        if (!Files.exists(rawPath)) {
            return
        }

        val rawSize = Files.size(rawPath)

        // 압축 도중 중단돼도 깨진 압축 파일이 남지 않도록 임시 파일에 쓴 뒤 바꿔 넣는다
        val compressedPath = storageProperties.getVideoChatRawCompressedPath(videoId)
        val tempPath = compressedPath.resolveSibling("${compressedPath.fileName}.tmp")
        GZIPOutputStream(Files.newOutputStream(tempPath)).use { gzip ->
            Files.copy(rawPath, gzip)
        }
        Files.move(tempPath, compressedPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        Files.delete(rawPath)

        logger.info(
            "VideoChatRawCompressService: Compressed chat raw: videoId={}, rawSize={} bytes, compressedSize={} bytes",
            videoId,
            rawSize,
            Files.size(compressedPath)
        )
    }

}
