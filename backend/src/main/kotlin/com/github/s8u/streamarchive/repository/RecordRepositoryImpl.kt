package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.dto.AdminRecordSearchRequest
import com.github.s8u.streamarchive.entity.QChannel
import com.github.s8u.streamarchive.entity.QRecord
import com.github.s8u.streamarchive.entity.QVideo
import com.github.s8u.streamarchive.entity.Record
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

    override fun searchForAdmin(request: AdminRecordSearchRequest, pageable: Pageable): Page<Record> {
        val results = queryFactory
            .selectFrom(record)
            .leftJoin(record.channel, channel).fetchJoin()
            .leftJoin(record.video, video).fetchJoin()
            .where(
                request.channelName?.let { record.channel.name.containsIgnoreCase(it) },
                request.platformType?.let { record.platformType.eq(it) },
                request.isEnded?.let { record.isEnded.eq(it) },
                request.isCancelled?.let { record.isCancelled.eq(it) },
                request.createdAtFrom?.let { record.createdAt.goe(it) },
                request.createdAtTo?.let { record.createdAt.loe(it) }
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(record.createdAt.desc())
            .fetch()

        val total = queryFactory
            .select(record.count())
            .from(record)
            .leftJoin(video).on(record.videoId.eq(video.id))
            .leftJoin(channel).on(video.channelId.eq(channel.id))
            .where(
                request.channelName?.let { channel.name.containsIgnoreCase(it) },
                request.platformType?.let { record.platformType.eq(it) },
                request.isEnded?.let { record.isEnded.eq(it) },
                request.isCancelled?.let { record.isCancelled.eq(it) },
                request.createdAtFrom?.let { record.createdAt.goe(it) },
                request.createdAtTo?.let { record.createdAt.loe(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }
}
