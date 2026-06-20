package com.github.s8u.streamarchive.global.util

import org.slf4j.LoggerFactory
import org.springframework.web.client.RestClient
import java.io.InputStream

object ImageDownloadUtils {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val restClient = RestClient.create()

    fun downloadImage(url: String): InputStream? {
        return try {
            val bytes = restClient.get()
                .uri(url)
                .retrieve()
                .body(ByteArray::class.java)

            bytes?.inputStream()
        } catch (e: Exception) {
            logger.error("ImageDownloadUtils: Failed to download image from url: {}", url, e)
            null
        }
    }

}
