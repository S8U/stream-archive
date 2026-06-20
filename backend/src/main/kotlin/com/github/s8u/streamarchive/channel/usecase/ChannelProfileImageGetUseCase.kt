package com.github.s8u.streamarchive.channel.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.channel.service.ChannelAccessAssertService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.properties.StorageProperties
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files

/**
 * 채널 프로필 이미지 조회 (공개)
 */
@Service
class ChannelProfileImageGetUseCase(
    private val channelRepository: ChannelRepository,
    private val channelAccessAssertService: ChannelAccessAssertService,
    private val storageProperties: StorageProperties
) {

    @Transactional(readOnly = true)
    fun getByUuid(uuid: String): Resource {
        val channel = channelRepository.findByUuid(uuid) ?: throw BusinessException(
            "채널을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        channelAccessAssertService.assertAccessible(channel.contentPrivacy)

        val profilePath = storageProperties.getChannelProfilePath(channel.id!!)

        if (!Files.exists(profilePath)) {
            throw BusinessException("프로필 이미지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        return FileSystemResource(profilePath)
    }

}
