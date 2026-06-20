package com.github.s8u.streamarchive.recording

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.channel.repository.ChannelRepository
import com.github.s8u.streamarchive.global.service.TransactionRunner
import com.github.s8u.streamarchive.video.entity.Video
import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import com.github.s8u.streamarchive.video.repository.VideoRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 녹화 종료와 메타데이터 갱신이 같은 동영상을 동시에 수정하는 상황을 검증한다.
 *
 * 메타데이터 갱신 중에 종료가 끼어들어도 낙관적 락 재시도로 둘 다 반영된다.
 * 어느 쪽도 예외로 끝나지 않아야 한다.
 */
@SpringBootTest
@ActiveProfiles("test")
class RecordingMetadataConcurrencyTest @Autowired constructor(
    private val transactionRunner: TransactionRunner,
    private val videoRepository: VideoRepository,
    private val channelRepository: ChannelRepository
) {

    @Test
    fun `같은 동영상을 동시에 수정해도 낙관적 락 재시도로 둘 다 반영된다`() {
        val videoId = saveVideo()

        // 한쪽이 동영상을 읽은 뒤 잠시 멈춰, 다른 쪽이 먼저 커밋하도록 만들어 충돌을 유발한다
        val otherCommitted = CountDownLatch(1)
        val results = mutableListOf<Throwable?>()

        val slowUpdate = thread {
            results += runCatching {
                transactionRunner.runWithRetry {
                    val video = videoRepository.findById(videoId).orElseThrow()
                    // 읽은 직후 상대가 먼저 커밋할 때까지 기다려 stale 상태를 만든다 (재시도하면 통과)
                    otherCommitted.await(2, TimeUnit.SECONDS)
                    video.applyMetadata(fileSize = 100, duration = 10)
                    videoRepository.save(video)
                }
            }.exceptionOrNull()
        }

        val fastEnd = thread {
            results += runCatching {
                transactionRunner.runWithRetry {
                    val video = videoRepository.findById(videoId).orElseThrow()
                    video.changeTitle("종료 시점 제목")
                    videoRepository.save(video)
                }
            }.exceptionOrNull()
            otherCommitted.countDown()
        }

        slowUpdate.join()
        fastEnd.join()

        // 어느 쪽도 예외로 끝나지 않는다 (충돌은 재시도로 흡수)
        assertTrue(results.all { it == null }, "동시 수정이 예외 없이 끝나야 한다: $results")

        // 두 수정이 모두 반영됐다 (제목은 종료가, 파일 정보는 갱신이 남긴다)
        val saved = videoRepository.findById(videoId).orElseThrow()
        assertEquals("종료 시점 제목", saved.title)
        assertEquals(100, saved.fileSize)
        assertEquals(10, saved.duration)
    }

    private fun saveVideo(): Long {
        val channel = channelRepository.save(
            Channel(
                uuid = UUID.randomUUID().toString(),
                name = "채널",
                contentPrivacy = ChannelContentPrivacy.PUBLIC
            )
        )
        val video = Video(
            uuid = UUID.randomUUID().toString(),
            channelId = channel.id!!,
            title = "원본 제목",
            contentPrivacy = VideoContentPrivacy.PUBLIC
        )
        return videoRepository.save(video).id!!
    }

}
