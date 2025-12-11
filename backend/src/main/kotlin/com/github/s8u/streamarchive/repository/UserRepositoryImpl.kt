package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.dto.AdminUserSearchRequest
import com.github.s8u.streamarchive.entity.QUser
import com.github.s8u.streamarchive.entity.User
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

    override fun searchForAdmin(request: AdminUserSearchRequest, pageable: Pageable): Page<User> {
        val results = queryFactory
            .selectFrom(user)
            .where(
                request.keyword?.let {
                    user.username.containsIgnoreCase(it)
                        .or(user.name.containsIgnoreCase(it))
                },
                request.role?.let { user.role.eq(it) },
                request.createdAtFrom?.let { user.createdAt.goe(it) },
                request.createdAtTo?.let { user.createdAt.loe(it) }
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(user.createdAt.desc())
            .fetch()

        val total = queryFactory
            .select(user.count())
            .from(user)
            .where(
                request.keyword?.let {
                    user.username.containsIgnoreCase(it)
                        .or(user.name.containsIgnoreCase(it))
                },
                request.role?.let { user.role.eq(it) },
                request.createdAtFrom?.let { user.createdAt.goe(it) },
                request.createdAtTo?.let { user.createdAt.loe(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }
}
