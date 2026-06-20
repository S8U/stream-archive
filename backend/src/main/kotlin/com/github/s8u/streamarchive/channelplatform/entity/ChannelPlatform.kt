package com.github.s8u.streamarchive.channelplatform.entity

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.global.entity.base.BaseSoftDeleteEntity
import com.github.s8u.streamarchive.platform.enums.PlatformType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(
    name = "channel_platforms",
    indexes = [
        Index(name = "idx_channel_platforms_platform_type", columnList = "platform_type"),
        Index(name = "idx_channel_platforms_is_active", columnList = "is_active")
    ]
)
@SQLRestriction("is_active = true")
@Comment("채널-플랫폼 연동")
class ChannelPlatform(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    @Comment("채널")
    val channel: Channel? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("플랫폼 유형")
    val platformType: PlatformType,

    platformChannelId: String,
    isSyncProfile: Boolean = true
) : BaseSoftDeleteEntity() {

    @Column(nullable = false, length = 255)
    @Comment("플랫폼 채널 ID")
    var platformChannelId: String = platformChannelId
        protected set

    @Column(nullable = false)
    @Comment("프로필 동기화 여부")
    var isSyncProfile: Boolean = isSyncProfile
        protected set

    /**
     * 채널-플랫폼 연동 정보를 수정한다.
     *
     * null인 인자는 기존 값을 유지한다.
     */
    fun update(platformChannelId: String?, isSyncProfile: Boolean?) {
        platformChannelId?.let { this.platformChannelId = it }
        isSyncProfile?.let { this.isSyncProfile = it }
    }

}
