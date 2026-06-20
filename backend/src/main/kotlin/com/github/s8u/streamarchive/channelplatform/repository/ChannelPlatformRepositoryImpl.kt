package com.github.s8u.streamarchive.channelplatform.repository

import com.github.s8u.streamarchive.channel.entity.QChannel
import com.github.s8u.streamarchive.channelplatform.entity.ChannelPlatform
import com.github.s8u.streamarchive.channelplatform.entity.QChannelPlatform
import com.github.s8u.streamarchive.channelplatform.usecase.dto.command.ChannelPlatformAdminSearchCommand
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class ChannelPlatformRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : ChannelPlatformRepositoryCustom {

    private val channelPlatform = QChannelPlatform.channelPlatform
    private val channel = QChannel.channel

    override fun searchForAdmin(command: ChannelPlatformAdminSearchCommand, pageable: Pageable): Page<ChannelPlatform> {
        val results = queryFactory
            .selectFrom(channelPlatform)
            .leftJoin(channelPlatform.channel, channel).fetchJoin()
            .where(
                command.id?.let { channelPlatform.id.eq(it) },
                command.channelName?.let { channel.name.containsIgnoreCase(it) },
                command.platformType?.let { channelPlatform.platformType.eq(it) },
                command.platformChannelId?.let { channelPlatform.platformChannelId.containsIgnoreCase(it) },
                command.isSyncProfile?.let { channelPlatform.isSyncProfile.eq(it) }
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(channelPlatform.createdAt.desc())
            .fetch()

        val total = queryFactory
            .select(channelPlatform.count())
            .from(channelPlatform)
            .leftJoin(channel).on(channelPlatform.channel.id.eq(channel.id))
            .where(
                command.id?.let { channelPlatform.id.eq(it) },
                command.channelName?.let { channel.name.containsIgnoreCase(it) },
                command.platformType?.let { channelPlatform.platformType.eq(it) },
                command.platformChannelId?.let { channelPlatform.platformChannelId.containsIgnoreCase(it) },
                command.isSyncProfile?.let { channelPlatform.isSyncProfile.eq(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }

}
