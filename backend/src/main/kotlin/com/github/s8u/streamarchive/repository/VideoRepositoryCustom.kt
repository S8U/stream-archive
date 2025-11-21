package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.dto.AdminVideoSearchRequest
import com.github.s8u.streamarchive.entity.Video
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface VideoRepositoryCustom {
    fun searchForAdmin(request: AdminVideoSearchRequest, pageable: Pageable): Page<Video>
}
