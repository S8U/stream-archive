package com.github.s8u.streamarchive.global.entity.base

import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * 기본 시간 엔티티
 *
 * 수정 이력이 필요 없는 엔티티가 상속한다.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("ID")
    var id: Long? = null
        protected set

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Comment("생성 일시")
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set

}
