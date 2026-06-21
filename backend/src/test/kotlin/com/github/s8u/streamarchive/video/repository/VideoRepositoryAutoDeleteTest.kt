package com.github.s8u.streamarchive.video.repository

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import jakarta.persistence.EntityManager
import java.time.LocalDateTime
import kotlin.test.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VideoRepositoryAutoDeleteTest @Autowired constructor(
    private val videoRepository: VideoRepository,
    private val channelRepository: ChannelRepository,
    private val entityManager: EntityManager
) {

    @Nested
    inner class FindAutoDeleteTargets {

        @Test
        fun `활성 상태이고 소장하지 않은 기준일 이전 동영상만 조회한다`() {
            val channel = channelRepository.save(channel(uuid = "channel-1"))
            val otherChannel = channelRepository.save(channel(uuid = "channel-2"))
            val oldestTarget = video(uuid = "oldest-target", channelId = channel.id!!)
            val newestTarget = video(uuid = "newest-target", channelId = channel.id!!)
            val recentVideo = video(uuid = "recent-video", channelId = channel.id!!)
            val archivedVideo = video(uuid = "archived-video", channelId = channel.id!!)
            val deletedVideo = video(uuid = "deleted-video", channelId = channel.id!!)
            val otherChannelVideo = video(uuid = "other-channel", channelId = otherChannel.id!!)
            oldestTarget.applyMetadata(fileSize = 1_000L, duration = 10)
            newestTarget.applyMetadata(fileSize = 2_000L, duration = 20)
            archivedVideo.archive(userId = null, ip = null)
            deletedVideo.softDelete(userId = null, ip = null)
            videoRepository.saveAll(
                listOf(oldestTarget, newestTarget, recentVideo, archivedVideo, deletedVideo, otherChannelVideo)
            )
            entityManager.flush()
            setCreatedAt(oldestTarget, CUTOFF.minusDays(2))
            setCreatedAt(newestTarget, CUTOFF.minusDays(1))
            setCreatedAt(recentVideo, CUTOFF.plusDays(1))
            setCreatedAt(archivedVideo, CUTOFF.minusDays(3))
            setCreatedAt(deletedVideo, CUTOFF.minusDays(3))
            setCreatedAt(otherChannelVideo, CUTOFF.minusDays(3))
            entityManager.clear()

            val targets = videoRepository.findAutoDeleteTargets(channel.id!!, CUTOFF)

            assertEquals(listOf("oldest-target", "newest-target"), targets.map { it.uuid })
            assertEquals(2L, videoRepository.countAutoDeleteTargets(channel.id!!, CUTOFF))
            assertEquals(3_000L, videoRepository.sumFileSizeAutoDeleteTargets(channel.id!!, CUTOFF))
        }
    }

    private fun channel(uuid: String): Channel {
        return Channel(
            uuid = uuid,
            name = uuid,
            contentPrivacy = ChannelContentPrivacy.PUBLIC
        )
    }

    private fun video(
        uuid: String,
        channelId: Long
    ): Video {
        return Video(
            uuid = uuid,
            channelId = channelId,
            title = uuid,
            contentPrivacy = VideoContentPrivacy.PUBLIC
        )
    }

    private fun setCreatedAt(
        video: Video,
        createdAt: LocalDateTime
    ) {
        entityManager.createNativeQuery("UPDATE videos SET created_at = :createdAt WHERE id = :id")
            .setParameter("createdAt", createdAt)
            .setParameter("id", video.id!!)
            .executeUpdate()
    }

    companion object {
        private val CUTOFF = LocalDateTime.of(2026, 6, 1, 0, 0)
    }

}
