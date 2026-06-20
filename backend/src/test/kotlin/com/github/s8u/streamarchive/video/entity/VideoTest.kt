package com.github.s8u.streamarchive.video.entity

import com.github.s8u.streamarchive.video.enums.VideoContentPrivacy
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VideoTest {

    private fun video(): Video {
        return Video(
            uuid = "video-uuid",
            channelId = 1L,
            title = "원래 제목",
            contentPrivacy = VideoContentPrivacy.PUBLIC,
            chatSyncOffsetMillis = 0
        )
    }

    @Nested
    inner class Update {

        @Test
        fun `모든 인자를 넘기면 전부 갱신된다`() {
            val video = video()

            video.update(
                title = "새 제목",
                description = "새 설명",
                contentPrivacy = VideoContentPrivacy.PRIVATE,
                chatSyncOffsetMillis = 1500
            )

            assertEquals("새 제목", video.title)
            assertEquals("새 설명", video.description)
            assertEquals(VideoContentPrivacy.PRIVATE, video.contentPrivacy)
            assertEquals(1500, video.chatSyncOffsetMillis)
        }

        @Test
        fun `title만 넘기면 title만 바뀌고 나머지는 유지된다`() {
            val video = video()

            video.update(title = "새 제목", description = null, contentPrivacy = null, chatSyncOffsetMillis = null)

            assertEquals("새 제목", video.title)
            assertNull(video.description)
            assertEquals(VideoContentPrivacy.PUBLIC, video.contentPrivacy)
            assertEquals(0, video.chatSyncOffsetMillis)
        }

        @Test
        fun `모든 인자가 null이면 아무 값도 바뀌지 않는다`() {
            val video = video()

            video.update(title = null, description = null, contentPrivacy = null, chatSyncOffsetMillis = null)

            assertEquals("원래 제목", video.title)
            assertNull(video.description)
            assertEquals(VideoContentPrivacy.PUBLIC, video.contentPrivacy)
        }
    }

    @Nested
    inner class ChangeTitle {

        @Test
        fun `제목을 바꾸면 title이 갱신된다`() {
            val video = video()

            video.changeTitle("바뀐 제목")

            assertEquals("바뀐 제목", video.title)
        }
    }

    @Nested
    inner class ApplyMetadata {

        @Test
        fun `메타데이터를 적용하면 파일 크기와 재생 시간이 갱신된다`() {
            val video = video()

            video.applyMetadata(fileSize = 2048L, duration = 360)

            assertEquals(2048L, video.fileSize)
            assertEquals(360, video.duration)
        }
    }

    @Nested
    inner class Archive {

        @Test
        fun `소장 처리하면 isArchived가 true가 되고 처리 정보가 채워진다`() {
            val video = video()

            video.archive(userId = 7L, ip = "127.0.0.1")

            assertTrue(video.isArchived)
            assertNotNull(video.archivedAt)
            assertEquals(7L, video.archivedBy)
            assertEquals("127.0.0.1", video.archivedIp)
        }
    }

    @Nested
    inner class Unarchive {

        @Test
        fun `소장을 해제하면 isArchived가 false가 되고 처리 정보가 비워진다`() {
            val video = video()
            video.archive(userId = 7L, ip = "127.0.0.1")

            video.unarchive()

            assertFalse(video.isArchived)
            assertNull(video.archivedAt)
            assertNull(video.archivedBy)
            assertNull(video.archivedIp)
        }
    }

    @Nested
    inner class InitialState {

        @Test
        fun `생성 직후에는 소장되지 않은 상태다`() {
            val video = video()

            assertFalse(video.isArchived)
            assertNull(video.archivedAt)
            assertNull(video.archivedBy)
            assertNull(video.archivedIp)
            assertEquals(0, video.duration)
            assertEquals(0L, video.fileSize)
        }
    }
}
