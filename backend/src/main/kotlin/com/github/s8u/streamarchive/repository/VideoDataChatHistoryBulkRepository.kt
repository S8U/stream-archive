package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.chat.ChatMessageDto
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.LocalDateTime

@Repository
class VideoDataChatHistoryBulkRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    /**
     * 채팅 메시지 배치 저장
     */
    fun bulkInsert(items: List<ChatMessageDto>) {
        if (items.isEmpty()) return

        val sql = """
            INSERT INTO video_data_chat_histories
            (video_id, username, message, data, offset_millis, created_at)
            VALUES (?, ?, ?, NULL, ?, ?)
        """.trimIndent()

        jdbcTemplate.batchUpdate(sql, object : BatchPreparedStatementSetter {
            override fun setValues(ps: PreparedStatement, i: Int) {
                val item = items[i]
                ps.setLong(1, item.videoId)
                ps.setString(2, item.username.take(255)) // max length
                ps.setString(3, item.message.take(1000)) // max length
                ps.setLong(4, item.offsetMillis)
                ps.setTimestamp(5, Timestamp.valueOf(item.createdAt))
            }

            override fun getBatchSize(): Int = items.size
        })
    }

}