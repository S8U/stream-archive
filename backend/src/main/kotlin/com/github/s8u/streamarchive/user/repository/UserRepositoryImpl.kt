package com.github.s8u.streamarchive.user.repository

import com.github.s8u.streamarchive.user.entity.QUser
import com.github.s8u.streamarchive.user.entity.User
import com.github.s8u.streamarchive.user.usecase.dto.command.UserAdminSearchCommand
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : UserRepositoryCustom {

    private val user = QUser.user

    override fun searchForAdmin(command: UserAdminSearchCommand, pageable: Pageable): Page<User> {
        val results = queryFactory
            .selectFrom(user)
            .where(
                command.id?.let { user.id.eq(it) },
                command.username?.let { user.username.containsIgnoreCase(it) },
                command.name?.let { user.name.containsIgnoreCase(it) },
                command.role?.let { user.role.eq(it) },
                command.createdAtFrom?.let { user.createdAt.goe(it) },
                command.createdAtTo?.let { user.createdAt.loe(it) }
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(user.createdAt.desc())
            .fetch()

        val total = queryFactory
            .select(user.count())
            .from(user)
            .where(
                command.id?.let { user.id.eq(it) },
                command.username?.let { user.username.containsIgnoreCase(it) },
                command.name?.let { user.name.containsIgnoreCase(it) },
                command.role?.let { user.role.eq(it) },
                command.createdAtFrom?.let { user.createdAt.goe(it) },
                command.createdAtTo?.let { user.createdAt.loe(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }

}
