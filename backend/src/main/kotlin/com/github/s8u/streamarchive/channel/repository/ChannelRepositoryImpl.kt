package com.github.s8u.streamarchive.channel.repository

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.entity.QChannel
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelAdminSearchCommand
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelSearchCommand
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
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

    override fun searchForAdmin(command: ChannelAdminSearchCommand, pageable: Pageable): Page<Channel> {
        val results = queryFactory
            .selectFrom(channel)
            .where(
                command.id?.let { channel.id.eq(it) },
                command.uuid?.let { channel.uuid.containsIgnoreCase(it) },
                command.name?.let { channel.name.containsIgnoreCase(it) },
                command.contentPrivacy?.let { channel.contentPrivacy.eq(it) }
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(channel.id.desc())
            .fetch()

        val total = queryFactory
            .select(channel.count())
            .from(channel)
            .where(
                command.id?.let { channel.id.eq(it) },
                command.uuid?.let { channel.uuid.containsIgnoreCase(it) },
                command.name?.let { channel.name.containsIgnoreCase(it) },
                command.contentPrivacy?.let { channel.contentPrivacy.eq(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }

    override fun searchForPublic(command: ChannelSearchCommand, pageable: Pageable): Page<Channel> {
        val results = queryFactory
            .selectFrom(channel)
            .where(
                channel.contentPrivacy.eq(ChannelContentPrivacy.PUBLIC),
                command.name?.let { channel.name.containsIgnoreCase(it) }
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(channel.name.asc(), channel.id.asc())
            .fetch()

        val total = queryFactory
            .select(channel.count())
            .from(channel)
            .where(
                channel.contentPrivacy.eq(ChannelContentPrivacy.PUBLIC),
                command.name?.let { channel.name.containsIgnoreCase(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }

}
