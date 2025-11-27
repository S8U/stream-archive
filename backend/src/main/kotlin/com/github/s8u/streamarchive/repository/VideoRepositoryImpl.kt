package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.dto.AdminVideoSearchRequest
import com.github.s8u.streamarchive.dto.PublicVideoSearchRequest
import com.github.s8u.streamarchive.entity.QChannel
import com.github.s8u.streamarchive.entity.QRecord
import com.github.s8u.streamarchive.entity.QVideo
import com.github.s8u.streamarchive.entity.Video
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class VideoRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : VideoRepositoryCustom {

    private val video = QVideo.video
    private val channel = QChannel.channel
    private val record = QRecord.record

    override fun searchForAdmin(request: AdminVideoSearchRequest, pageable: Pageable): Page<Video> {
        val results = queryFactory
            .selectFrom(video)
            .leftJoin(channel).on(video.channelId.eq(channel.id))
            .where(
                request.title?.let { video.title.containsIgnoreCase(it) },
                request.channelName?.let { channel.name.containsIgnoreCase(it) },
                request.contentPrivacy?.let { video.contentPrivacy.eq(it) },
                request.createdAtFrom?.let { video.createdAt.goe(it) },
                request.createdAtTo?.let { video.createdAt.loe(it) }
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(video.createdAt.desc())
            .fetch()

        val total = queryFactory
            .select(video.count())
            .from(video)
            .leftJoin(channel).on(video.channelId.eq(channel.id))
            .where(
                request.title?.let { video.title.containsIgnoreCase(it) },
                request.channelName?.let { channel.name.containsIgnoreCase(it) },
                request.contentPrivacy?.let { video.contentPrivacy.eq(it) },
                request.createdAtFrom?.let { video.createdAt.goe(it) },
                request.createdAtTo?.let { video.createdAt.loe(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }

    override fun searchForPublic(request: PublicVideoSearchRequest, pageable: Pageable): Page<Video> {
        val results = queryFactory
            .selectFrom(video)
            .leftJoin(channel).on(video.channelId.eq(channel.id)).fetchJoin()
            .leftJoin(record).on(video.id.eq(record.videoId)).fetchJoin()
            .where(
                video.contentPrivacy.eq(ContentPrivacy.PUBLIC),
                video.isActive.eq(true),
                request.title?.let { video.title.containsIgnoreCase(it) },
                request.channelName?.let { channel.name.containsIgnoreCase(it) }
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(video.createdAt.desc())
            .fetch()

        val total = queryFactory
            .select(video.count())
            .from(video)
            .leftJoin(channel).on(video.channelId.eq(channel.id))
            .where(
                video.contentPrivacy.eq(ContentPrivacy.PUBLIC),
                video.isActive.eq(true),
                request.title?.let { video.title.containsIgnoreCase(it) },
                request.channelName?.let { channel.name.containsIgnoreCase(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }
}
