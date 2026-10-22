package com.github.s8u.streamarchive.user.service

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.user.entity.UserSetting
import com.github.s8u.streamarchive.user.enums.UserSettingKey
import com.github.s8u.streamarchive.user.repository.UserSettingRepository
import com.github.s8u.streamarchive.user.service.dto.UserSettingLoadResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserSettingLoadServiceTest {

    private val userSettingRepository = mockk<UserSettingRepository>()
    private val currentUserService = mockk<CurrentUserService>()
    private val userSettingLoadService = UserSettingLoadService(userSettingRepository, currentUserService)

    @Nested
    inner class Load {

        @Test
        fun `비로그인이면 저장소를 보지 않고 기본값을 쓴다`() {
            every { currentUserService.getCurrentUserId() } returns null
            every { currentUserService.isAdmin() } returns false

            val result = userSettingLoadService.load()

            assertEquals(UserSettingLoadResult(isShowUnlistedChannels = false, isShowUnlistedVideos = false), result)
            verify(exactly = 0) { userSettingRepository.findAllByUserId(any()) }
        }

        @Test
        fun `관리자는 저장한 값을 쓰고 저장하지 않은 설정은 기본값을 쓴다`() {
            every { currentUserService.getCurrentUserId() } returns USER_ID
            every { currentUserService.isAdmin() } returns true
            every { userSettingRepository.findAllByUserId(USER_ID) } returns listOf(
                setting(UserSettingKey.SHOW_UNLISTED_VIDEOS, "true")
            )

            val result = userSettingLoadService.load()

            assertEquals(UserSettingLoadResult(isShowUnlistedChannels = false, isShowUnlistedVideos = true), result)
        }

        @Test
        fun `관리자가 아니면 저장한 관리자 전용 설정도 기본값을 쓴다`() {
            every { currentUserService.getCurrentUserId() } returns USER_ID
            every { currentUserService.isAdmin() } returns false
            every { userSettingRepository.findAllByUserId(USER_ID) } returns listOf(
                setting(UserSettingKey.SHOW_UNLISTED_CHANNELS, "true"),
                setting(UserSettingKey.SHOW_UNLISTED_VIDEOS, "true")
            )

            val result = userSettingLoadService.load()

            assertEquals(UserSettingLoadResult(isShowUnlistedChannels = false, isShowUnlistedVideos = false), result)
        }
    }

    private fun setting(settingKey: UserSettingKey, settingValue: String): UserSetting {
        return UserSetting(
            userId = USER_ID,
            settingKey = settingKey,
            settingValue = settingValue
        )
    }

    companion object {
        private const val USER_ID = 1L
    }

}
