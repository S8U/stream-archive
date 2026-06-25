package com.github.s8u.streamarchive.channel.repository

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.repository.dto.ChannelAdminSearchProjection
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelAdminSearchCommand
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelSearchCommand
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ChannelRepositoryCustom {

    fun searchForAdmin(command: ChannelAdminSearchCommand, pageable: Pageable): Page<ChannelAdminSearchProjection>
    fun searchForPublic(command: ChannelSearchCommand, pageable: Pageable): Page<Channel>

}
