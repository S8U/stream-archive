package com.github.s8u.streamarchive.video.repository

import com.github.s8u.streamarchive.video.entity.VideoChatHistory
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.sql.Types

@Repository
class VideoChatHistoryBulkRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    fun bulkInsert(items: List<VideoChatHistory>) {
        if (items.isEmpty()) return

        val sql = """
            INSERT INTO video_chat_histories
            (video_id, message_type, username, user_role, message, donation_amount, emojis, data,
             offset_millis, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, NULL, ?, ?)
        """.trimIndent()

        jdbcTemplate.batchUpdate(sql, object : BatchPreparedStatementSetter {
            override fun setValues(ps: PreparedStatement, i: Int) {
                val item = items[i]
                ps.setLong(1, item.videoId)
                ps.setString(2, item.messageType.name)
                ps.setString(3, item.username.take(255)) // 최대 길이
                ps.setString(4, item.userRole?.name)
                ps.setString(5, item.message.take(1000)) // 최대 길이
                ps.setObject(6, item.donationAmount, Types.INTEGER)
                ps.setString(7, item.emojis)
                ps.setLong(8, item.offsetMillis)
                ps.setTimestamp(9, Timestamp.valueOf(item.createdAt))
            }

            override fun getBatchSize(): Int = items.size
        })
    }

}
