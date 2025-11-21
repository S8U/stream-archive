package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.dto.AdminRecordScheduleSearchRequest
import com.github.s8u.streamarchive.entity.QChannel
import com.github.s8u.streamarchive.entity.QChannelPlatform
import com.github.s8u.streamarchive.entity.QRecordSchedule
import com.github.s8u.streamarchive.entity.RecordSchedule
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class RecordScheduleRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : RecordScheduleRepositoryCustom {

    private val recordSchedule = QRecordSchedule.recordSchedule
    private val channelPlatform = QChannelPlatform.channelPlatform
    private val channel = QChannel.channel

    override fun searchForAdmin(request: AdminRecordScheduleSearchRequest, pageable: Pageable): Page<RecordSchedule> {
        val results = queryFactory
            .selectFrom(recordSchedule)
            .leftJoin(channelPlatform).on(
                recordSchedule.channelId.eq(channelPlatform.channelId)
                    .and(recordSchedule.platformType.eq(channelPlatform.platformType))
            )
            .leftJoin(channel).on(channelPlatform.channelId.eq(channel.id))
            .where(
                request.channelName?.let { channel.name.containsIgnoreCase(it) },
                request.platformType?.let { recordSchedule.platformType.eq(it) },
                request.scheduleType?.let { recordSchedule.scheduleType.eq(it) },
                request.recordQuality?.let { recordSchedule.recordQuality.eq(it) }
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(recordSchedule.createdAt.desc())
            .fetch()

        val total = queryFactory
            .select(recordSchedule.count())
            .from(recordSchedule)
            .leftJoin(channelPlatform).on(
                recordSchedule.channelId.eq(channelPlatform.channelId)
                    .and(recordSchedule.platformType.eq(channelPlatform.platformType))
            )
            .leftJoin(channel).on(channelPlatform.channelId.eq(channel.id))
            .where(
                request.channelName?.let { channel.name.containsIgnoreCase(it) },
                request.platformType?.let { recordSchedule.platformType.eq(it) },
                request.scheduleType?.let { recordSchedule.scheduleType.eq(it) },
                request.recordQuality?.let { recordSchedule.recordQuality.eq(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }
}
