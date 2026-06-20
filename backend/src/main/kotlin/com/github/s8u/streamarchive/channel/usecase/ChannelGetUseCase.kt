package com.github.s8u.streamarchive.channel.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.channel.service.ChannelAccessAssertService
import com.github.s8u.streamarchive.channel.usecase.dto.result.ChannelGetResult
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널 단건 조회 (공개)
 */
@Service
class ChannelGetUseCase(
    private val channelRepository: ChannelRepository,
    private val channelAccessAssertService: ChannelAccessAssertService,
    private val urlService: UrlService
) {

    @Transactional(readOnly = true)
    fun getByUuid(uuid: String): ChannelGetResult {
        val channel = channelRepository.findByUuid(uuid) ?: throw BusinessException(
            "채널을 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
        )

        channelAccessAssertService.assertAccessible(channel.contentPrivacy)

        return ChannelGetResult.from(
            channel = channel,
            profileUrl = urlService.channelProfileUrl(channel.uuid)
        )
    }

}
