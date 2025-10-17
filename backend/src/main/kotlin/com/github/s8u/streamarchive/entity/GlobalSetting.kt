package com.github.s8u.streamarchive.entity

import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "global_settings",
    indexes = [
        Index(name = "idx_global_settings_key", columnList = "settingKey")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Comment("전역 설정")
class GlobalSetting(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("전역 설정 ID")
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 100)
    @Comment("설정 키")
    val settingKey: String,

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    @Comment("설정 값")
    var settingValue: String,

    @Column(length = 500)
    @Comment("설명")
    var description: String? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Comment("생성 일시")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false)
    @Comment("수정 일시")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)