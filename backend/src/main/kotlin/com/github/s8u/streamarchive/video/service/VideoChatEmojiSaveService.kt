package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.global.properties.StorageProperties
import com.github.s8u.streamarchive.global.util.ImageDownloadUtils
import com.github.s8u.streamarchive.video.service.dto.VideoChatEmojiSaveResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

/**
 * 동영상 채팅 이모지 저장 서비스
 */
@Service
class VideoChatEmojiSaveService(
    private val storageProperties: StorageProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun saveAll(videoId: Long, emojis: Map<String, String>): List<VideoChatEmojiSaveResult> {
        if (emojis.isEmpty()) return emptyList()

        return emojis
            .mapNotNull { (placeholder, imageUrl) -> save(videoId, placeholder, imageUrl) }
    }

    private fun save(
        videoId: Long,
        placeholder: String,
        imageUrl: String
    ): VideoChatEmojiSaveResult? {
        if (!isAllowedUrl(imageUrl)) {
            logger.warn(
                "VideoChatEmojiSaveService: Emoji URL is not allowed: videoId={}, url={}",
                videoId,
                imageUrl
            )
            return null
        }

        val filename = getFilename(placeholder, imageUrl)
        val filePath = storageProperties.getVideoEmojiPath(videoId, filename)

        if (Files.exists(filePath)) {
            return VideoChatEmojiSaveResult(
                placeholder = placeholder,
                filename = filename
            )
        }

        try {
            val inputStream = ImageDownloadUtils.downloadImage(imageUrl) ?: return null

            Files.createDirectories(filePath.parent)
            inputStream.use {
                Files.copy(it, filePath, StandardCopyOption.REPLACE_EXISTING)
            }

            logger.debug("VideoChatEmojiSaveService: Saved chat emoji: videoId={}, filename={}", videoId, filename)

            return VideoChatEmojiSaveResult(
                placeholder = placeholder,
                filename = filename
            )
        } catch (e: Exception) {
            logger.warn(
                "VideoChatEmojiSaveService: Failed to save chat emoji: videoId={}, url={}",
                videoId,
                imageUrl,
                e
            )
            return null
        }
    }

    private fun getFilename(placeholder: String, imageUrl: String): String {
        val name = placeholder
            .removePrefix("{:")
            .removeSuffix(":}")
            .replace(Regex("[^A-Za-z0-9_-]"), "_")
            .ifBlank { "emoji" }
        val hash = sha256(imageUrl).take(12)
        val extension = getExtension(imageUrl)

        return "$name-$hash.$extension"
    }

    private fun getExtension(url: String): String {
        val path = try {
            URI(url).path
        } catch (e: Exception) {
            ""
        }
        val extension = path.substringAfterLast('.', missingDelimiterValue = "")
            .lowercase()

        return when (extension) {
            "png", "gif", "webp", "jpg", "jpeg" -> extension
            else -> "png"
        }
    }

    private fun isAllowedUrl(url: String): Boolean {
        val uri = try {
            URI(url)
        } catch (e: Exception) {
            return false
        }
        val scheme = uri.scheme?.lowercase()
        return (scheme == "https" || scheme == "http") && !uri.host.isNullOrBlank()
    }

    private fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())

        return bytes.joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
    }

}
