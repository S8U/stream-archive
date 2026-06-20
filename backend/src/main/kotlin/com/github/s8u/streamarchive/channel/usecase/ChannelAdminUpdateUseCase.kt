package com.github.s8u.streamarchive.channel.usecase

import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelAdminUpdateCommand
import com.github.s8u.streamarchive.channel.usecase.dto.result.ChannelAdminUpdateResult
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.global.service.UrlService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널 수정 (관리자)
 */
@Service
class ChannelAdminUpdateUseCase(
    private val channelRepository: ChannelRepository,
    private val urlService: UrlService
) {

    @Transactional
    fun update(id: Long, command: ChannelAdminUpdateCommand): ChannelAdminUpdateResult {
        val channel = channelRepository.findById(id).orElseThrow {
            BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        channel.update(command.name, command.contentPrivacy)

        return ChannelAdminUpdateResult.from(
            channel = channel,
            profileUrl = urlService.channelProfileUrl(channel.uuid)
        )
    }

}
