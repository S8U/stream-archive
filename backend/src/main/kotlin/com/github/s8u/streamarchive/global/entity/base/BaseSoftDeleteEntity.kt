package com.github.s8u.streamarchive.global.entity.base

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.Comment
import java.time.LocalDateTime

/**
 * 소프트 삭제를 지원하는 엔티티
 *
 * 실제로 행을 지우지 않고 비활성화한다.
 * 비활성 행을 조회에서 제외하는 처리는 상속하는 엔티티가 직접 둔다.
 */
@MappedSuperclass
abstract class BaseSoftDeleteEntity : BaseEntity() {

    @Column(nullable = false)
    @Comment("활성 상태")
    var isActive: Boolean = true
        protected set

    @Comment("삭제 일시")
    var deletedAt: LocalDateTime? = null
        protected set

    @Comment("삭제한 사용자 ID")
    var deletedBy: Long? = null
        protected set

    @Column(length = 45)
    @Comment("삭제 시 IP")
    var deletedIp: String? = null
        protected set

    /**
     * 소프트 삭제 처리한다.
     */
    fun softDelete(userId: Long?, ip: String?) {
        isActive = false
        deletedAt = LocalDateTime.now()
        deletedBy = userId
        deletedIp = ip
    }

}
