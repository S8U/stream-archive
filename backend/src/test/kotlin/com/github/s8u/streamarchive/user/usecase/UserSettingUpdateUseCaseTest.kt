package com.github.s8u.streamarchive.user.usecase

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.user.entity.UserSetting
import com.github.s8u.streamarchive.user.enums.UserSettingKey
import com.github.s8u.streamarchive.user.repository.UserSettingRepository
import com.github.s8u.streamarchive.user.service.UserSettingLoadService
import com.github.s8u.streamarchive.user.service.dto.UserSettingLoadResult
import com.github.s8u.streamarchive.user.usecase.dto.command.UserSettingUpdateCommand
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus

class UserSettingUpdateUseCaseTest {

    private val userSettingRepository = mockk<UserSettingRepository>()
    private val currentUserService = mockk<CurrentUserService>()
    private val userSettingLoadService = mockk<UserSettingLoadService>()
    private val userSettingUpdateUseCase = UserSettingUpdateUseCase(
        userSettingRepository,
        currentUserService,
        userSettingLoadService
    )

    @Nested
    inner class Update {

        @Test
        fun `로그인한 사용자가 없으면 예외를 던진다`() {
            every { currentUserService.getCurrentUserId() } returns null

            val exception = assertThrows<BusinessException> {
                userSettingUpdateUseCase.update(UserSettingUpdateCommand(isShowUnlistedVideos = true))
            }

            assertEquals(HttpStatus.UNAUTHORIZED, exception.status)
        }

        @Test
        fun `관리자가 아니면 관리자 전용 설정을 바꿀 수 없다`() {
            every { currentUserService.getCurrentUserId() } returns USER_ID
            every { currentUserService.isAdmin() } returns false

            val exception = assertThrows<BusinessException> {
                userSettingUpdateUseCase.update(UserSettingUpdateCommand(isShowUnlistedVideos = true))
            }

            assertEquals(HttpStatus.FORBIDDEN, exception.status)
            verify(exactly = 0) { userSettingRepository.save(any()) }
        }

        @Test
        fun `저장한 적 없는 설정은 새로 만들고 저장한 설정은 값을 바꾼다`() {
            val savedSetting = UserSetting(
                userId = USER_ID,
                settingKey = UserSettingKey.SHOW_UNLISTED_CHANNELS,
                settingValue = "false"
            )
            val newSetting = slot<UserSetting>()
            every { currentUserService.getCurrentUserId() } returns USER_ID
            every { currentUserService.isAdmin() } returns true
            every { userSettingRepository.findAllByUserId(USER_ID) } returns listOf(savedSetting)
            every { userSettingRepository.save(capture(newSetting)) } answers { newSetting.captured }
            every { userSettingLoadService.load() } returns UserSettingLoadResult(
                isShowUnlistedChannels = true,
                isShowUnlistedVideos = true
            )

            val result = userSettingUpdateUseCase.update(
                UserSettingUpdateCommand(isShowUnlistedChannels = true, isShowUnlistedVideos = true)
            )

            assertEquals("true", savedSetting.settingValue)
            assertEquals(UserSettingKey.SHOW_UNLISTED_VIDEOS, newSetting.captured.settingKey)
            assertEquals("true", newSetting.captured.settingValue)
            verify(exactly = 1) { userSettingRepository.save(any()) }
            assertEquals(true, result.isShowUnlistedChannels)
            assertEquals(true, result.isShowUnlistedVideos)
        }

        @Test
        fun `요청에 없는 설정은 건드리지 않는다`() {
            every { currentUserService.getCurrentUserId() } returns USER_ID
            every { currentUserService.isAdmin() } returns true
            every { userSettingRepository.findAllByUserId(USER_ID) } returns emptyList()
            every { userSettingRepository.save(any()) } answers { firstArg() }
            every { userSettingLoadService.load() } returns UserSettingLoadResult(
                isShowUnlistedChannels = false,
                isShowUnlistedVideos = true
            )

            userSettingUpdateUseCase.update(UserSettingUpdateCommand(isShowUnlistedVideos = true))

            verify(exactly = 1) {
                userSettingRepository.save(match { it.settingKey == UserSettingKey.SHOW_UNLISTED_VIDEOS })
            }
        }
    }

    companion object {
        private const val USER_ID = 1L
    }

}
