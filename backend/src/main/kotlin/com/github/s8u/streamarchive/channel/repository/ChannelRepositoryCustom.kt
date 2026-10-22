package com.github.s8u.streamarchive.channel.repository

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.channel.repository.dto.ChannelAdminSearchProjection
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelAdminSearchCommand
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelSearchCommand
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ChannelRepositoryCustom {

    fun searchForAdmin(command: ChannelAdminSearchCommand, pageable: Pageable): Page<ChannelAdminSearchProjection>

    // 공개 목록 (channelPrivacies에 든 공개 범위만)
    fun searchForPublic(
        command: ChannelSearchCommand,
        channelPrivacies: Collection<ChannelContentPrivacy>,
        pageable: Pageable
    ): Page<Channel>

}
