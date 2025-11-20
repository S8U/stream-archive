package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.dto.AdminChannelPlatformSearchRequest
import com.github.s8u.streamarchive.entity.ChannelPlatform
import com.github.s8u.streamarchive.entity.QChannel
import com.github.s8u.streamarchive.entity.QChannelPlatform
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

    override fun search(request: AdminChannelPlatformSearchRequest, pageable: Pageable): Page<ChannelPlatform> {
        val results = queryFactory
            .selectFrom(channelPlatform)
            .leftJoin(channel).on(channelPlatform.channelId.eq(channel.id))
            .where(
                request.channelName?.let { channel.name.containsIgnoreCase(it) },
                request.platformType?.let { channelPlatform.platformType.eq(it) },
                request.platformChannelId?.let { channelPlatform.platformChannelId.containsIgnoreCase(it) },
                request.isSyncProfile?.let { channelPlatform.isSyncProfile.eq(it) }
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(channelPlatform.createdAt.desc())
            .fetch()

        val total = queryFactory
            .select(channelPlatform.count())
            .from(channelPlatform)
            .leftJoin(channel).on(channelPlatform.channelId.eq(channel.id))
            .where(
                request.channelName?.let { channel.name.containsIgnoreCase(it) },
                request.platformType?.let { channelPlatform.platformType.eq(it) },
                request.platformChannelId?.let { channelPlatform.platformChannelId.containsIgnoreCase(it) },
                request.isSyncProfile?.let { channelPlatform.isSyncProfile.eq(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }
}
