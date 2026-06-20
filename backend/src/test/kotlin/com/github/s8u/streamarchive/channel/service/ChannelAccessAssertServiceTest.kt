package com.github.s8u.streamarchive.channel.service

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.global.exception.BusinessException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ChannelAccessAssertServiceTest {

    private val currentUserService = mockk<CurrentUserService>()
    private val channelAccessAssertService = ChannelAccessAssertService(currentUserService)

    @Test
    fun `PRIVATE 채널은 비관리자가 접근하면 예외를 던진다`() {
        every { currentUserService.isAdmin() } returns false

        assertThrows<BusinessException> {
            channelAccessAssertService.assertAccessible(ChannelContentPrivacy.PRIVATE)
        }
    }

    @Test
    fun `PRIVATE 채널이라도 관리자는 접근할 수 있다`() {
        every { currentUserService.isAdmin() } returns true

        channelAccessAssertService.assertAccessible(ChannelContentPrivacy.PRIVATE)
    }

    @Test
    fun `PUBLIC 채널은 비관리자도 접근할 수 있다`() {
        every { currentUserService.isAdmin() } returns false

        channelAccessAssertService.assertAccessible(ChannelContentPrivacy.PUBLIC)
    }

}
