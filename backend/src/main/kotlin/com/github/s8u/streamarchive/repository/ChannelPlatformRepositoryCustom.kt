package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.dto.AdminChannelPlatformSearchRequest
import com.github.s8u.streamarchive.entity.ChannelPlatform
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ChannelPlatformRepositoryCustom {
    fun search(request: AdminChannelPlatformSearchRequest, pageable: Pageable): Page<ChannelPlatform>
}
