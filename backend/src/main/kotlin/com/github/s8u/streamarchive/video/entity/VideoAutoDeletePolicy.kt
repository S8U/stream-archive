package com.github.s8u.streamarchive.video.entity

import com.github.s8u.streamarchive.global.entity.base.BaseEntity
import com.github.s8u.streamarchive.global.exception.BusinessException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Comment
import org.springframework.http.HttpStatus

@Entity
@Table(
    name = "video_auto_delete_policies",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_video_auto_delete_policies_channel_id", columnNames = ["channelId"])
    ],
    indexes = [
        Index(name = "idx_video_auto_delete_policies_channel_id", columnList = "channelId")
    ]
)
@Comment("동영상 자동 삭제 정책")
class VideoAutoDeletePolicy(
    @Column
    @Comment("채널 ID")
    val channelId: Long? = null,

    isEnabled: Boolean = false,
    deleteAfterDays: Int
) : BaseEntity() {

    @Column(nullable = false)
    @Comment("자동 삭제 활성화 여부")
    var isEnabled: Boolean = isEnabled
        protected set

    @Column(nullable = false)
    @Comment("생성 후 며칠 지난 동영상을 삭제할지")
    var deleteAfterDays: Int = deleteAfterDays
        protected set

    init {
        validateDeleteAfterDays(deleteAfterDays)
    }

    /**
     * 정책을 수정한다.
     *
     * null인 인자는 기존 값을 유지한다.
     */
    fun update(
        isEnabled: Boolean?,
        deleteAfterDays: Int?
    ) {
        isEnabled?.let { this.isEnabled = it }
        deleteAfterDays?.let {
            validateDeleteAfterDays(it)
            this.deleteAfterDays = it
        }
    }

    private fun validateDeleteAfterDays(deleteAfterDays: Int) {
        if (deleteAfterDays < 1)
            throw BusinessException("삭제 기준 일수는 1일 이상이어야 합니다.", HttpStatus.BAD_REQUEST)
    }

}
