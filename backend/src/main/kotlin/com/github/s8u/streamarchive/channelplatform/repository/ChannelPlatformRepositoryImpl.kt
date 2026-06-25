package com.github.s8u.streamarchive.channelplatform.repository

import com.github.s8u.streamarchive.channel.entity.QChannel
import com.github.s8u.streamarchive.channelplatform.entity.ChannelPlatform
import com.github.s8u.streamarchive.channelplatform.entity.QChannelPlatform
import com.github.s8u.streamarchive.channelplatform.usecase.dto.command.ChannelPlatformAdminSearchCommand
import com.github.s8u.streamarchive.global.util.QueryDslOrderUtils
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
            .orderBy(
                *QueryDslOrderUtils.getOrderSpecifiers(
                    pageable = pageable,
                    orderBuilders = mapOf(
                        "id" to { isAscending ->
                            if (isAscending) channelPlatform.id.asc() else channelPlatform.id.desc()
                        },
                        "channelName" to { isAscending ->
                            if (isAscending) channel.name.asc() else channel.name.desc()
                        },
                        "platformType" to { isAscending ->
                            if (isAscending) channelPlatform.platformType.asc() else channelPlatform.platformType.desc()
                        },
                        "platformChannelId" to { isAscending ->
                            if (isAscending) {
                                channelPlatform.platformChannelId.asc()
                            } else {
                                channelPlatform.platformChannelId.desc()
                            }
                        },
                        "isSyncProfile" to { isAscending ->
                            if (isAscending) {
                                channelPlatform.isSyncProfile.asc()
                            } else {
                                channelPlatform.isSyncProfile.desc()
                            }
                        },
                        "createdAt" to { isAscending ->
                            if (isAscending) channelPlatform.createdAt.asc() else channelPlatform.createdAt.desc()
                        },
                        "updatedAt" to { isAscending ->
                            if (isAscending) channelPlatform.updatedAt.asc() else channelPlatform.updatedAt.desc()
                        }
                    ),
                    defaultOrders = listOf(channelPlatform.id.desc()),
                    tieBreaker = channelPlatform.id.desc()
                )
            )
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
