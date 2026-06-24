package com.github.s8u.streamarchive.video.repository

import com.github.s8u.streamarchive.video.entity.VideoChatHistory
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement
import java.sql.Timestamp

@Repository
class VideoChatHistoryBulkRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    fun bulkInsert(items: List<VideoChatHistory>) {
        if (items.isEmpty()) return

        val sql = """
            INSERT INTO video_chat_histories
            (video_id, username, message, emojis, data, offset_millis, created_at)
            VALUES (?, ?, ?, ?, NULL, ?, ?)
        """.trimIndent()

        jdbcTemplate.batchUpdate(sql, object : BatchPreparedStatementSetter {
            override fun setValues(ps: PreparedStatement, i: Int) {
                val item = items[i]
                ps.setLong(1, item.videoId)
                ps.setString(2, item.username.take(255)) // 최대 길이
                ps.setString(3, item.message.take(1000)) // 최대 길이
                ps.setString(4, item.emojis)
                ps.setLong(5, item.offsetMillis)
                ps.setTimestamp(6, Timestamp.valueOf(item.createdAt))
            }

            override fun getBatchSize(): Int = items.size
        })
    }

}
