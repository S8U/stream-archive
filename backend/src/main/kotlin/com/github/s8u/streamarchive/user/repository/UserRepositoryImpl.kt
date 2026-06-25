package com.github.s8u.streamarchive.user.repository

import com.github.s8u.streamarchive.global.util.QueryDslOrderUtils
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
            .orderBy(
                *QueryDslOrderUtils.getOrderSpecifiers(
                    pageable = pageable,
                    orderBuilders = mapOf(
                        "id" to { isAscending -> if (isAscending) user.id.asc() else user.id.desc() },
                        "username" to { isAscending -> if (isAscending) user.username.asc() else user.username.desc() },
                        "name" to { isAscending -> if (isAscending) user.name.asc() else user.name.desc() },
                        "role" to { isAscending -> if (isAscending) user.role.asc() else user.role.desc() },
                        "lastLoginAt" to { isAscending ->
                            if (isAscending) user.lastLoginAt.asc() else user.lastLoginAt.desc()
                        },
                        "createdAt" to { isAscending ->
                            if (isAscending) user.createdAt.asc() else user.createdAt.desc()
                        },
                        "updatedAt" to { isAscending ->
                            if (isAscending) user.updatedAt.asc() else user.updatedAt.desc()
                        }
                    ),
                    defaultOrders = listOf(user.id.desc()),
                    tieBreaker = user.id.desc()
                )
            )
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
