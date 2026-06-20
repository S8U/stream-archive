package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * 동영상 접근 권한을 검증한다.
 *
 * 동영상 자체와 소속 채널 모두 접근 가능해야 한다.
 * 소속 채널 정보가 없으면(channelPrivacy == null) 접근할 수 없다.
 */
@Service
class VideoAccessAssertService(
    private val currentUserService: CurrentUserService
) {

    fun assertAccessible(videoPrivacy: VideoContentPrivacy, channelPrivacy: ChannelContentPrivacy?) {
        if (videoPrivacy == VideoContentPrivacy.PRIVATE && !currentUserService.isAdmin()) {
            throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        if (channelPrivacy == null) {
            throw BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        if (channelPrivacy == ChannelContentPrivacy.PRIVATE && !currentUserService.isAdmin()) {
            throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
    }

}
