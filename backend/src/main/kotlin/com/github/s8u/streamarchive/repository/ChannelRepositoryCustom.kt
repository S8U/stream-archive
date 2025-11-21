package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.dto.AdminChannelSearchRequest
import com.github.s8u.streamarchive.entity.Channel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ChannelRepositoryCustom {
    fun searchForAdmin(request: AdminChannelSearchRequest, pageable: Pageable): Page<Channel>
}