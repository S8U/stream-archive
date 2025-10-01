package com.github.s8u.streamarchive.domain.entity

import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "channel_settings",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_channel_setting", columnNames = ["channel_id", "setting_key"])
    ],
    indexes = [
        Index(name = "idx_channel_settings_channel_id", columnList = "channelId")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Comment("채널별 설정")
class ChannelSetting(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("채널별 설정 ID")
    val id: Long? = null,

    @Column(nullable = false)
    @Comment("채널 ID")
    val channelId: Long,

    @Column(nullable = false, length = 100)
    @Comment("설정 키")
    val settingKey: String,

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    @Comment("설정 값")
    var settingValue: String,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Comment("생성 일시")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false)
    @Comment("수정 일시")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)