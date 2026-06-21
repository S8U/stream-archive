package com.github.s8u.streamarchive.video.service

import com.github.s8u.streamarchive.video.entity.VideoAutoDeletePolicy
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VideoAutoDeleteTargetGetServiceTest {

    private val videoAutoDeleteTargetGetService = VideoAutoDeleteTargetGetService()

    @Nested
    inner class GetTargets {

        @Test
        fun `채널 정책이 있으면 전체 정책보다 우선한다`() {
            val globalPolicy = policy(channelId = null, isEnabled = true, deleteAfterDays = 30)
            val channelPolicy = policy(channelId = CHANNEL_ID, isEnabled = true, deleteAfterDays = 7)

            val targets = videoAutoDeleteTargetGetService.getTargets(
                globalPolicy = globalPolicy,
                channelPolicies = listOf(channelPolicy),
                channelIds = listOf(CHANNEL_ID),
                now = NOW
            )

            assertEquals(1, targets.size)
            assertEquals(7, targets.single().deleteAfterDays)
            assertEquals(NOW.minusDays(7), targets.single().createdBefore)
        }

        @Test
        fun `채널 정책이 없으면 전체 정책을 적용한다`() {
            val globalPolicy = policy(channelId = null, isEnabled = true, deleteAfterDays = 30)

            val targets = videoAutoDeleteTargetGetService.getTargets(
                globalPolicy = globalPolicy,
                channelPolicies = emptyList(),
                channelIds = listOf(CHANNEL_ID),
                now = NOW
            )

            assertEquals(1, targets.size)
            assertEquals(30, targets.single().deleteAfterDays)
            assertEquals(NOW.minusDays(30), targets.single().createdBefore)
        }

        @Test
        fun `비활성 채널 정책은 활성 전체 정책을 덮어쓰고 대상을 제외한다`() {
            val globalPolicy = policy(channelId = null, isEnabled = true, deleteAfterDays = 30)
            val channelPolicy = policy(channelId = CHANNEL_ID, isEnabled = false, deleteAfterDays = 7)

            val targets = videoAutoDeleteTargetGetService.getTargets(
                globalPolicy = globalPolicy,
                channelPolicies = listOf(channelPolicy),
                channelIds = listOf(CHANNEL_ID),
                now = NOW
            )

            assertEquals(emptyList(), targets)
        }

        @Test
        fun `적용할 정책이 없는 채널은 대상에서 제외한다`() {
            val targets = videoAutoDeleteTargetGetService.getTargets(
                globalPolicy = null,
                channelPolicies = emptyList(),
                channelIds = listOf(CHANNEL_ID),
                now = NOW
            )

            assertEquals(emptyList(), targets)
        }
    }

    private fun policy(
        channelId: Long?,
        isEnabled: Boolean,
        deleteAfterDays: Int
    ): VideoAutoDeletePolicy {
        return VideoAutoDeletePolicy(
            channelId = channelId,
            isEnabled = isEnabled,
            deleteAfterDays = deleteAfterDays
        )
    }

    companion object {
        private const val CHANNEL_ID = 1L
        private val NOW = LocalDateTime.of(2026, 6, 21, 12, 0)
    }

}
