package com.github.s8u.streamarchive.channel.entity

import com.github.s8u.streamarchive.global.entity.base.BaseSoftDeleteEntity
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(
    name = "channels",
    indexes = [
        Index(name = "idx_channels_content_privacy", columnList = "content_privacy"),
        Index(name = "idx_channels_created_at", columnList = "created_at"),
        Index(name = "idx_channels_is_active", columnList = "is_active")
    ]
)
@SQLRestriction("is_active = true")
@Comment("채널")
class Channel(
    @Column(nullable = false, unique = true, length = 36)
    @Comment("채널 UUID")
    val uuid: String,

    name: String,
    contentPrivacy: ChannelContentPrivacy
) : BaseSoftDeleteEntity() {

    @Column(nullable = false, length = 255)
    @Comment("채널 이름")
    var name: String = name
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "content_privacy", nullable = false)
    @Comment("콘텐츠 공개 범위")
    var contentPrivacy: ChannelContentPrivacy = contentPrivacy
        protected set

    /**
     * 채널 정보를 수정한다.
     *
     * null인 인자는 기존 값을 유지한다.
     */
    fun update(name: String?, contentPrivacy: ChannelContentPrivacy?) {
        name?.let { this.name = it }
        contentPrivacy?.let { this.contentPrivacy = it }
    }

}
