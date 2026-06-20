package com.github.s8u.streamarchive.global.entity.base

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

/**
 * 감사 필드를 가지는 엔티티
 *
 * 수정 일시와 생성·수정 주체(사용자 ID, IP)를 기록한다.
 * 생성·수정 주체는 [recordCreator]·[recordUpdater]로만 채운다.
 */
@MappedSuperclass
abstract class BaseEntity : BaseTimeEntity() {

    @LastModifiedDate
    @Column(nullable = false)
    @Comment("수정 일시")
    var updatedAt: LocalDateTime = LocalDateTime.now()
        protected set

    @Comment("생성한 사용자 ID")
    var createdBy: Long? = null
        protected set

    @Column(length = 45)
    @Comment("생성 시 IP")
    var createdIp: String? = null
        protected set

    @Comment("수정한 사용자 ID")
    var updatedBy: Long? = null
        protected set

    @Column(length = 45)
    @Comment("수정 시 IP")
    var updatedIp: String? = null
        protected set

    /**
     * 생성 주체를 기록한다.
     */
    fun recordCreator(userId: Long?, ip: String?) {
        createdBy = userId
        createdIp = ip
        updatedBy = userId
        updatedIp = ip
    }

    /**
     * 수정 주체를 기록한다.
     */
    fun recordUpdater(userId: Long?, ip: String?) {
        updatedBy = userId
        updatedIp = ip
    }

}
