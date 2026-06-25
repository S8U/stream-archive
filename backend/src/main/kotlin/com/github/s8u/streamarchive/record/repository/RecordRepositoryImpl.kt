package com.github.s8u.streamarchive.record.repository

import com.github.s8u.streamarchive.channel.entity.QChannel
import com.github.s8u.streamarchive.global.util.QueryDslOrderUtils
import com.github.s8u.streamarchive.record.entity.QRecord
import com.github.s8u.streamarchive.record.entity.Record
import com.github.s8u.streamarchive.record.usecase.dto.command.RecordAdminSearchCommand
import com.github.s8u.streamarchive.video.entity.QVideo
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class RecordRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : RecordRepositoryCustom {

    private val record = QRecord.record
    private val video = QVideo.video
    private val channel = QChannel.channel

    override fun searchForAdmin(command: RecordAdminSearchCommand, pageable: Pageable): Page<Record> {
        val results = queryFactory
            .selectFrom(record)
            // FIXME: 임시로 삭제된 채널 녹화건은 안보이도록함
            .join(record.channel, channel).fetchJoin()
            // FIXME: 임시로 삭제된 동영상 녹화건은 안보이도록함
            .join(record.video, video).fetchJoin()
            .where(
                command.id?.let { record.id.eq(it) },
                command.channelName?.let { record.channel.name.containsIgnoreCase(it) },
                command.title?.let { record.video.title.containsIgnoreCase(it) },
                command.platformStreamId?.let { record.platformStreamId.containsIgnoreCase(it) },
                command.platformType?.let { record.platformType.eq(it) },
                command.isEnded?.let { record.isEnded.eq(it) },
                command.isCancelled?.let { record.isCancelled.eq(it) },
                command.createdAtFrom?.let { record.createdAt.goe(it) },
                command.createdAtTo?.let { record.createdAt.loe(it) }
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(
                *QueryDslOrderUtils.getOrderSpecifiers(
                    pageable = pageable,
                    orderBuilders = mapOf(
                        "id" to { isAscending -> if (isAscending) record.id.asc() else record.id.desc() },
                        "channelName" to { isAscending ->
                            if (isAscending) channel.name.asc() else channel.name.desc()
                        },
                        "platformType" to { isAscending ->
                            if (isAscending) record.platformType.asc() else record.platformType.desc()
                        },
                        "title" to { isAscending -> if (isAscending) video.title.asc() else video.title.desc() },
                        "platformStreamId" to { isAscending ->
                            if (isAscending) record.platformStreamId.asc() else record.platformStreamId.desc()
                        },
                        "recordQuality" to { isAscending ->
                            if (isAscending) record.recordQuality.asc() else record.recordQuality.desc()
                        },
                        "createdAt" to { isAscending ->
                            if (isAscending) record.createdAt.asc() else record.createdAt.desc()
                        },
                        "endedAt" to { isAscending -> if (isAscending) record.endedAt.asc() else record.endedAt.desc() }
                    ),
                    defaultOrders = listOf(record.id.desc()),
                    tieBreaker = record.id.desc()
                )
            )
            .fetch()

        val total = queryFactory
            .select(record.count())
            .from(record)
            .join(record.channel, channel)
            .join(record.video, video)
            .where(
                command.id?.let { record.id.eq(it) },
                command.channelName?.let { channel.name.containsIgnoreCase(it) },
                command.title?.let { video.title.containsIgnoreCase(it) },
                command.platformStreamId?.let { record.platformStreamId.containsIgnoreCase(it) },
                command.platformType?.let { record.platformType.eq(it) },
                command.isEnded?.let { record.isEnded.eq(it) },
                command.isCancelled?.let { record.isCancelled.eq(it) },
                command.createdAtFrom?.let { record.createdAt.goe(it) },
                command.createdAtTo?.let { record.createdAt.loe(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }

}
