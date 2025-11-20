package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.dto.AdminChannelSearchRequest
import com.github.s8u.streamarchive.entity.Channel
import com.github.s8u.streamarchive.entity.QChannel
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class ChannelRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : ChannelRepositoryCustom {

    private val channel = QChannel.channel

    override fun search(request: AdminChannelSearchRequest, pageable: Pageable): Page<Channel> {
        val results = queryFactory
            .selectFrom(channel)
            .where(
                request.name?.let { channel.name.containsIgnoreCase(it) },
                request.contentPrivacy?.let { channel.contentPrivacy.eq(it) }
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(channel.createdAt.desc())
            .fetch()

        val total = queryFactory
            .select(channel.count())
            .from(channel)
            .where(
                request.name?.let { channel.name.containsIgnoreCase(it) },
                request.contentPrivacy?.let { channel.contentPrivacy.eq(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }
}