package com.github.s8u.streamarchive.channel.usecase

import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelSearchCommand
import com.github.s8u.streamarchive.channel.usecase.dto.result.ChannelSearchResult
import com.github.s8u.streamarchive.global.service.UrlService
import com.github.s8u.streamarchive.user.service.UserSettingLoadService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 채널 목록 조회 (공개)
 */
@Service
class ChannelSearchUseCase(
    private val channelRepository: ChannelRepository,
    private val urlService: UrlService,
    private val userSettingLoadService: UserSettingLoadService
) {

    @Transactional(readOnly = true)
    fun search(command: ChannelSearchCommand, pageable: Pageable): Page<ChannelSearchResult> {
        // 공개 채널만 보여주되, 관리자가 켠 설정에 따라 일부공개 채널도 넣는다.
        val userSetting = userSettingLoadService.load()
        val channelPrivacies = ChannelContentPrivacy.getListedPrivacies(userSetting.isShowUnlistedChannels)

        return channelRepository.searchForPublic(command, channelPrivacies, pageable)
            .map { channel ->
                ChannelSearchResult.from(
                    channel = channel,
                    profileUrl = urlService.channelProfileUrl(channel.uuid)
                )
            }
    }

}
