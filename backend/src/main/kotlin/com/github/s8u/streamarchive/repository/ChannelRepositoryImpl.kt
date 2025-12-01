package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.dto.AdminChannelSearchRequest
import com.github.s8u.streamarchive.dto.PublicChannelSearchRequest
import com.github.s8u.streamarchive.entity.Channel
import com.github.s8u.streamarchive.entity.QChannel
import com.github.s8u.streamarchive.enums.ContentPrivacy
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

    override fun searchForAdmin(request: AdminChannelSearchRequest, pageable: Pageable): Page<Channel> {
        val results = queryFactory
            .selectFrom(channel)
            .where(
                request.id?.let { channel.id.eq(it) },
                request.uuid?.let { channel.uuid.containsIgnoreCase(it) },
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
                request.id?.let { channel.id.eq(it) },
                request.uuid?.let { channel.uuid.containsIgnoreCase(it) },
                request.name?.let { channel.name.containsIgnoreCase(it) },
                request.contentPrivacy?.let { channel.contentPrivacy.eq(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }

    override fun searchForPublic(request: PublicChannelSearchRequest, pageable: Pageable): Page<Channel> {
        val results = queryFactory
            .selectFrom(channel)
            .where(
                channel.contentPrivacy.eq(ContentPrivacy.PUBLIC),
                request.name?.let { channel.name.containsIgnoreCase(it) }
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(channel.createdAt.desc())
            .fetch()

        val total = queryFactory
            .select(channel.count())
            .from(channel)
            .where(
                channel.contentPrivacy.eq(ContentPrivacy.PUBLIC),
                request.name?.let { channel.name.containsIgnoreCase(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }
}
