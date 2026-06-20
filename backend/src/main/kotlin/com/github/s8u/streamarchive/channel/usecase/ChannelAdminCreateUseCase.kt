package com.github.s8u.streamarchive.channel.usecase

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelAdminCreateCommand
import com.github.s8u.streamarchive.channel.usecase.dto.result.ChannelAdminCreateResult
import com.github.s8u.streamarchive.global.service.UrlService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 채널 생성 (관리자)
 */
@Service
class ChannelAdminCreateUseCase(
    private val channelRepository: ChannelRepository,
    private val urlService: UrlService
) {

    @Transactional
    fun create(command: ChannelAdminCreateCommand): ChannelAdminCreateResult {
        val channel = Channel(
            uuid = UUID.randomUUID().toString(),
            name = command.name,
            contentPrivacy = command.contentPrivacy
        )
        val saved = channelRepository.save(channel)

        return ChannelAdminCreateResult.from(
            channel = saved,
            profileUrl = urlService.channelProfileUrl(saved.uuid)
        )
    }

}
