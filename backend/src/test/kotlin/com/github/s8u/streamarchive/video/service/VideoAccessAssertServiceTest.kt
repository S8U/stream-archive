package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VideoAccessAssertServiceTest {

    private val currentUserService = mockk<CurrentUserService>()
    private val videoAccessAssertService = VideoAccessAssertService(currentUserService)

    @Test
    fun `PRIVATE 동영상은 비관리자가 접근하면 예외를 던진다`() {
        every { currentUserService.isAdmin() } returns false

        assertThrows<BusinessException> {
            videoAccessAssertService.assertAccessible(
                VideoContentPrivacy.PRIVATE,
                ChannelContentPrivacy.PUBLIC
            )
        }
    }

    @Test
    fun `소속 채널 정보가 없으면 예외를 던진다`() {
        every { currentUserService.isAdmin() } returns false

        assertThrows<BusinessException> {
            videoAccessAssertService.assertAccessible(VideoContentPrivacy.PUBLIC, null)
        }
    }

    @Test
    fun `PRIVATE 채널의 동영상은 비관리자가 접근하면 예외를 던진다`() {
        every { currentUserService.isAdmin() } returns false

        assertThrows<BusinessException> {
            videoAccessAssertService.assertAccessible(
                VideoContentPrivacy.PUBLIC,
                ChannelContentPrivacy.PRIVATE
            )
        }
    }

    @Test
    fun `PUBLIC 동영상과 PUBLIC 채널은 비관리자도 접근할 수 있다`() {
        every { currentUserService.isAdmin() } returns false

        videoAccessAssertService.assertAccessible(
            VideoContentPrivacy.PUBLIC,
            ChannelContentPrivacy.PUBLIC
        )
    }

    @Test
    fun `PRIVATE 동영상과 PRIVATE 채널이라도 관리자는 접근할 수 있다`() {
        every { currentUserService.isAdmin() } returns true

        videoAccessAssertService.assertAccessible(
            VideoContentPrivacy.PRIVATE,
            ChannelContentPrivacy.PRIVATE
        )
    }

}
