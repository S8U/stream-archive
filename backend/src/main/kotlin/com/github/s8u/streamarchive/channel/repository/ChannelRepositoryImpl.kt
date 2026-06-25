package com.github.s8u.streamarchive.channel.repository

import com.github.s8u.streamarchive.channel.entity.Channel
import com.github.s8u.streamarchive.channel.entity.QChannel
import com.github.s8u.streamarchive.channel.repository.dto.ChannelAdminSearchProjection
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelAdminSearchCommand
import com.github.s8u.streamarchive.channel.usecase.dto.command.ChannelSearchCommand
import com.github.s8u.streamarchive.channel.enums.ChannelContentPrivacy
import com.github.s8u.streamarchive.global.util.QueryDslOrderUtils
import com.github.s8u.streamarchive.video.entity.QVideo
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
    private val video = QVideo.video

    override fun searchForAdmin(command: ChannelAdminSearchCommand, pageable: Pageable): Page<ChannelAdminSearchProjection> {
        val totalVideoFileSize = video.fileSize.sum().coalesce(0L)

        val results = queryFactory
            .select(
                channel.id,
                channel.uuid,
                channel.name,
                totalVideoFileSize,
                channel.contentPrivacy,
                channel.createdAt,
                channel.updatedAt
            )
            .from(channel)
            .leftJoin(video).on(
                video.channelId.eq(channel.id),
                video.isActive.eq(true)
            )
            .where(
                command.id?.let { channel.id.eq(it) },
                command.uuid?.let { channel.uuid.containsIgnoreCase(it) },
                command.name?.let { channel.name.containsIgnoreCase(it) },
                command.contentPrivacy?.let { channel.contentPrivacy.eq(it) }
            )
            .groupBy(
                channel.id,
                channel.uuid,
                channel.name,
                channel.contentPrivacy,
                channel.createdAt,
                channel.updatedAt
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(
                *QueryDslOrderUtils.getOrderSpecifiers(
                    pageable = pageable,
                    orderBuilders = mapOf(
                        "id" to { isAscending -> if (isAscending) channel.id.asc() else channel.id.desc() },
                        "uuid" to { isAscending -> if (isAscending) channel.uuid.asc() else channel.uuid.desc() },
                        "name" to { isAscending -> if (isAscending) channel.name.asc() else channel.name.desc() },
                        "contentPrivacy" to { isAscending ->
                            if (isAscending) channel.contentPrivacy.asc() else channel.contentPrivacy.desc()
                        },
                        "totalVideoFileSize" to { isAscending ->
                            if (isAscending) totalVideoFileSize.asc() else totalVideoFileSize.desc()
                        },
                        "createdAt" to { isAscending ->
                            if (isAscending) channel.createdAt.asc() else channel.createdAt.desc()
                        },
                        "updatedAt" to { isAscending ->
                            if (isAscending) channel.updatedAt.asc() else channel.updatedAt.desc()
                        }
                    ),
                    defaultOrders = listOf(channel.id.desc()),
                    tieBreaker = channel.id.desc()
                )
            )
            .fetch()
            .map { tuple ->
                ChannelAdminSearchProjection(
                    id = tuple.get(channel.id)!!,
                    uuid = tuple.get(channel.uuid)!!,
                    name = tuple.get(channel.name)!!,
                    totalVideoFileSize = tuple.get(totalVideoFileSize) ?: 0L,
                    contentPrivacy = tuple.get(channel.contentPrivacy)!!,
                    createdAt = tuple.get(channel.createdAt)!!,
                    updatedAt = tuple.get(channel.updatedAt)!!
                )
            }

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
