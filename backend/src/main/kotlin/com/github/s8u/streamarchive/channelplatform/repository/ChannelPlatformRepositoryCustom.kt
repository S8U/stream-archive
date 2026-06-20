package com.github.s8u.streamarchive.channelplatform.repository

import com.github.s8u.streamarchive.channelplatform.entity.ChannelPlatform
import com.github.s8u.streamarchive.channelplatform.usecase.dto.command.ChannelPlatformAdminSearchCommand
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ChannelPlatformRepositoryCustom {

    fun searchForAdmin(command: ChannelPlatformAdminSearchCommand, pageable: Pageable): Page<ChannelPlatform>

}
