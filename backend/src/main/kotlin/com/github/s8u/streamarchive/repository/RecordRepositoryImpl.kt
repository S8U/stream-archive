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
            .join(record.channel, channel).fetchJoin() // FIXME: 임시로 삭제된 채널 녹화건은 안보이도록함
            .join(record.video, video).fetchJoin() // FIXME: 임시로 삭제된 동영상 녹화건은 안보이도록함
            .where(
                request.id?.let { record.id.eq(it) },
                request.channelName?.let { record.channel.name.containsIgnoreCase(it) },
                request.title?.let { record.video.title.containsIgnoreCase(it) },
                request.platformStreamId?.let { record.platformStreamId.containsIgnoreCase(it) },
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
                request.id?.let { record.id.eq(it) },
                request.channelName?.let { channel.name.containsIgnoreCase(it) },
                request.title?.let { video.title.containsIgnoreCase(it) },
                request.platformStreamId?.let { record.platformStreamId.containsIgnoreCase(it) },
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
