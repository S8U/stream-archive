package com.github.s8u.streamarchive.recordschedule.repository

import com.github.s8u.streamarchive.channel.entity.QChannel
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
            .orderBy(recordSchedule.createdAt.desc())
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
