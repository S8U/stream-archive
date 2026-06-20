package com.github.s8u.streamarchive.channel.service

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.global.exception.BusinessException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * 채널 접근 권한을 검증한다.
 *
 * 비공개(PRIVATE) 채널은 관리자만 접근할 수 있다.
 */
@Service
class ChannelAccessAssertService(
    private val currentUserService: CurrentUserService
) {

    fun assertAccessible(contentPrivacy: ChannelContentPrivacy) {
        if (contentPrivacy == ChannelContentPrivacy.PRIVATE && !currentUserService.isAdmin()) {
            throw BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
    }

}
