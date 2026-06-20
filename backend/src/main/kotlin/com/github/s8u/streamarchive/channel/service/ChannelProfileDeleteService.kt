package com.github.s8u.streamarchive.channel.service

import com.github.s8u.streamarchive.global.properties.StorageProperties
import org.springframework.stereotype.Service
import java.nio.file.Files

/**
 * 채널 프로필 이미지 파일을 삭제한다.
 */
@Service
class ChannelProfileDeleteService(
    private val storageProperties: StorageProperties
) {

    fun deleteProfile(channelId: Long) {
        val filePath = storageProperties.getChannelProfilePath(channelId)
        if (Files.exists(filePath)) {
            Files.delete(filePath)
        }
    }

}
