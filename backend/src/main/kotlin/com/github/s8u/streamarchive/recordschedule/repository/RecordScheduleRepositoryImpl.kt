package com.github.s8u.streamarchive.recordschedule.repository

import com.github.s8u.streamarchive.channel.entity.QChannel
import com.github.s8u.streamarchive.global.util.QueryDslOrderUtils
import com.github.s8u.streamarchive.recordschedule.entity.QRecordSchedule
import com.github.s8u.streamarchive.recordschedule.entity.RecordSchedule
import com.github.s8u.streamarchive.recordschedule.usecase.dto.command.RecordScheduleAdminSearchCommand
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
    private val channel = QChannel.channel

    override fun searchForAdmin(command: RecordScheduleAdminSearchCommand, pageable: Pageable): Page<RecordSchedule> {
        val results = queryFactory
            .selectFrom(recordSchedule)
            .leftJoin(recordSchedule.channel, channel).fetchJoin()
            .where(
                command.id?.let { recordSchedule.id.eq(it) },
                command.channelName?.let { recordSchedule.channel.name.containsIgnoreCase(it) },
                command.platformType?.let { recordSchedule.platformType.eq(it) },
                command.scheduleType?.let { recordSchedule.scheduleType.eq(it) },
                command.recordQuality?.let { recordSchedule.recordQuality.eq(it) }
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(
                *QueryDslOrderUtils.getOrderSpecifiers(
                    pageable = pageable,
                    orderBuilders = mapOf(
                        "id" to { isAscending ->
                            if (isAscending) recordSchedule.id.asc() else recordSchedule.id.desc()
                        },
                        "channelName" to { isAscending ->
                            if (isAscending) channel.name.asc() else channel.name.desc()
                        },
                        "platformType" to { isAscending ->
                            if (isAscending) recordSchedule.platformType.asc() else recordSchedule.platformType.desc()
                        },
                        "scheduleType" to { isAscending ->
                            if (isAscending) recordSchedule.scheduleType.asc() else recordSchedule.scheduleType.desc()
                        },
                        "value" to { isAscending ->
                            if (isAscending) recordSchedule.value.asc() else recordSchedule.value.desc()
                        },
                        "recordQuality" to { isAscending ->
                            if (isAscending) recordSchedule.recordQuality.asc() else recordSchedule.recordQuality.desc()
                        },
                        "priority" to { isAscending ->
                            if (isAscending) recordSchedule.priority.asc() else recordSchedule.priority.desc()
                        },
                        "autoArchive" to { isAscending ->
                            if (isAscending) recordSchedule.autoArchive.asc() else recordSchedule.autoArchive.desc()
                        },
                        "createdAt" to { isAscending ->
                            if (isAscending) recordSchedule.createdAt.asc() else recordSchedule.createdAt.desc()
                        },
                        "updatedAt" to { isAscending ->
                            if (isAscending) recordSchedule.updatedAt.asc() else recordSchedule.updatedAt.desc()
                        }
                    ),
                    defaultOrders = listOf(recordSchedule.id.desc()),
                    tieBreaker = recordSchedule.id.desc()
                )
            )
            .fetch()

        val total = queryFactory
            .select(recordSchedule.count())
            .from(recordSchedule)
            .leftJoin(recordSchedule.channel, channel)
            .where(
                command.id?.let { recordSchedule.id.eq(it) },
                command.channelName?.let { channel.name.containsIgnoreCase(it) },
                command.platformType?.let { recordSchedule.platformType.eq(it) },
                command.scheduleType?.let { recordSchedule.scheduleType.eq(it) },
                command.recordQuality?.let { recordSchedule.recordQuality.eq(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }

}
