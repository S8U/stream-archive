package com.github.s8u.streamarchive.video.usecase

import com.github.s8u.streamarchive.auth.security.service.CurrentUserService
import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.global.exception.BusinessException
import com.github.s8u.streamarchive.video.entity.VideoAutoDeletePolicy
import com.github.s8u.streamarchive.video.repository.VideoAutoDeletePolicyRepository
import com.github.s8u.streamarchive.video.usecase.dto.command.VideoAutoDeletePolicyUpdateCommand
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class VideoAutoDeletePolicyUpdateUseCaseTest {

    private val videoAutoDeletePolicyRepository = mockk<VideoAutoDeletePolicyRepository>()
    private val channelRepository = mockk<ChannelRepository>()
    private val currentUserService = mockk<CurrentUserService>()
    private val videoAutoDeletePolicyUpdateUseCase = VideoAutoDeletePolicyUpdateUseCase(
        videoAutoDeletePolicyRepository,
        channelRepository,
        currentUserService
    )

    @AfterEach
    fun tearDown() {
        RequestContextHolder.resetRequestAttributes()
    }

    @Nested
    inner class Update {

        @Test
        fun `존재하지 않는 채널 정책을 설정하면 예외를 던진다`() {
            every { channelRepository.existsById(CHANNEL_ID) } returns false

            val exception = assertThrows<BusinessException> {
                videoAutoDeletePolicyUpdateUseCase.update(command(channelId = CHANNEL_ID))
            }

            assertEquals(HttpStatus.NOT_FOUND, exception.status)
            verify(exactly = 0) { currentUserService.getCurrentUserId() }
            verify(exactly = 0) { videoAutoDeletePolicyRepository.save(any()) }
        }

        @Test
        fun `전체 정책이 없으면 생성하고 생성 주체를 기록한다`() {
            setRequestIp(CLIENT_IP)
            every { currentUserService.getCurrentUserId() } returns USER_ID
            every { videoAutoDeletePolicyRepository.findByChannelIdIsNull() } returns null
            val policySlot = slot<VideoAutoDeletePolicy>()
            every { videoAutoDeletePolicyRepository.save(capture(policySlot)) } answers { firstArg() }

            val result = videoAutoDeletePolicyUpdateUseCase.update(command(channelId = null))

            assertEquals(null, result.channelId)
            assertTrue(result.isEnabled)
            assertEquals(30, result.deleteAfterDays)
            assertEquals(USER_ID, policySlot.captured.createdBy)
            assertEquals(CLIENT_IP, policySlot.captured.createdIp)
            assertEquals(USER_ID, policySlot.captured.updatedBy)
            assertEquals(CLIENT_IP, policySlot.captured.updatedIp)
        }

        @Test
        fun `기존 채널 정책이면 값을 수정하고 수정 주체를 기록한다`() {
            setRequestIp(CLIENT_IP)
            val policy = VideoAutoDeletePolicy(
                channelId = CHANNEL_ID,
                isEnabled = true,
                deleteAfterDays = 30
            )
            every { channelRepository.existsById(CHANNEL_ID) } returns true
            every { currentUserService.getCurrentUserId() } returns USER_ID
            every { videoAutoDeletePolicyRepository.findByChannelId(CHANNEL_ID) } returns policy

            val result = videoAutoDeletePolicyUpdateUseCase.update(
                VideoAutoDeletePolicyUpdateCommand(
                    channelId = CHANNEL_ID,
                    isEnabled = false,
                    deleteAfterDays = 7
                )
            )

            assertFalse(result.isEnabled)
            assertEquals(7, result.deleteAfterDays)
            assertEquals(USER_ID, policy.updatedBy)
            assertEquals(CLIENT_IP, policy.updatedIp)
            verify(exactly = 0) { videoAutoDeletePolicyRepository.save(any()) }
        }
    }

    private fun command(channelId: Long?): VideoAutoDeletePolicyUpdateCommand {
        return VideoAutoDeletePolicyUpdateCommand(
            channelId = channelId,
            isEnabled = true,
            deleteAfterDays = 30
        )
    }

    private fun setRequestIp(ip: String) {
        val request = MockHttpServletRequest()
        request.addHeader("X-Forwarded-For", "$ip, 10.0.0.1")
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
    }

    companion object {
        private const val CHANNEL_ID = 1L
        private const val USER_ID = 99L
        private const val CLIENT_IP = "127.0.0.1"
    }

}
