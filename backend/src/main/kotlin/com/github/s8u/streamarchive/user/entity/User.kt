package com.github.s8u.streamarchive.user.entity

import com.github.s8u.streamarchive.global.entity.base.BaseSoftDeleteEntity
import com.github.s8u.streamarchive.user.enums.Role
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_users_username", columnList = "username"),
        Index(name = "idx_users_role", columnList = "role"),
        Index(name = "idx_users_is_active", columnList = "is_active")
    ]
)
@SQLRestriction("is_active = true")
@Comment("사용자")
class User(
    @Column(nullable = false, unique = true, length = 36)
    @Comment("사용자 UUID")
    val uuid: String,

    @Column(nullable = false, unique = true, length = 100)
    @Comment("사용자명")
    val username: String,

    name: String,
    password: String,
    role: Role = Role.USER
) : BaseSoftDeleteEntity() {

    @Column(nullable = false, length = 100)
    @Comment("이름")
    var name: String = name
        protected set

    @Column(nullable = false, length = 255)
    @Comment("비밀번호")
    var password: String = password
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("역할")
    var role: Role = role
        protected set

    @Column
    @Comment("마지막 로그인 일시")
    var lastLoginAt: LocalDateTime? = null
        protected set

    /**
     * 사용자 정보를 수정한다.
     *
     * null인 인자는 기존 값을 유지한다.
     */
    fun update(name: String?, role: Role?) {
        name?.let { this.name = it }
        role?.let { this.role = it }
    }

    /**
     * 이름을 수정한다.
     */
    fun updateName(name: String) {
        this.name = name
    }

    /**
     * 비밀번호를 변경한다.
     */
    fun changePassword(encodedPassword: String) {
        password = encodedPassword
    }

    /**
     * 마지막 로그인 일시를 현재 시각으로 갱신한다.
     */
    fun login() {
        lastLoginAt = LocalDateTime.now()
    }

}
