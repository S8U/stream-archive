package com.github.s8u.streamarchive.service

import com.github.s8u.streamarchive.entity.Channel
import com.github.s8u.streamarchive.entity.Video
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.github.s8u.streamarchive.exception.BusinessException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class ContentPrivacyService(
    private val authenticationService: AuthenticationService
) {
    fun assertCanAccessChannel(channel: Channel) {
        if (channel.contentPrivacy == ContentPrivacy.PRIVATE && !authenticationService.isAdmin()) {
            throw BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
    }

    fun assertCanAccessVideo(video: Video) {
        if (video.contentPrivacy == ContentPrivacy.PRIVATE && !authenticationService.isAdmin()) {
            throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }

        val channel = video.channel ?: throw BusinessException("채널을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        if (channel.contentPrivacy == ContentPrivacy.PRIVATE && !authenticationService.isAdmin()) {
            throw BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        }
    }
}
