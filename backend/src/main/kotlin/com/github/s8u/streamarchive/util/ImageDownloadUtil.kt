package com.github.s8u.streamarchive.util

import org.slf4j.LoggerFactory
import org.springframework.web.client.RestClient
import java.io.InputStream

object ImageDownloadUtil {
    private val logger = LoggerFactory.getLogger(ImageDownloadUtil::class.java)
    private val restClient = RestClient.create()

    fun downloadImage(url: String): InputStream? {
        return try {
            val bytes = restClient.get()
                .uri(url)
                .retrieve()
                .body(ByteArray::class.java)

            bytes?.inputStream()
        } catch (e: Exception) {
            logger.error("Failed to download image from url: {}", url, e)
            null
        }
    }
}