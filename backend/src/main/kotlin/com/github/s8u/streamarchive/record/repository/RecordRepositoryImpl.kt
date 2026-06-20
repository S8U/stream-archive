package com.github.s8u.streamarchive.record.repository

import com.github.s8u.streamarchive.channel.entity.QChannel
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
            .join(record.channel, channel).fetchJoin() // FIXME: 임시로 삭제된 채널 녹화건은 안보이도록함
            .join(record.video, video).fetchJoin() // FIXME: 임시로 삭제된 동영상 녹화건은 안보이도록함
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
            .orderBy(record.createdAt.desc())
            .fetch()

        val total = queryFactory
            .select(record.count())
            .from(record)
            .leftJoin(video).on(record.videoId.eq(video.id))
            .leftJoin(channel).on(video.channelId.eq(channel.id))
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
