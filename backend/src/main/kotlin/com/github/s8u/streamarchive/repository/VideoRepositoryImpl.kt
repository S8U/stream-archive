package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.dto.AdminDashboardVideoHistoriesResponse
import com.github.s8u.streamarchive.dto.AdminVideoSearchRequest
import com.github.s8u.streamarchive.dto.PublicVideoSearchRequest
import com.github.s8u.streamarchive.entity.QChannel
import com.github.s8u.streamarchive.entity.QRecord
import com.github.s8u.streamarchive.entity.QVideo
import com.github.s8u.streamarchive.entity.Video
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.querydsl.jpa.impl.JPAQueryFactory
import java.time.LocalDate
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
            .leftJoin(video.channel, channel).fetchJoin()
            .leftJoin(video.record, record).fetchJoin()
            .where(
                request.id?.let { video.id.eq(it) },
                request.uuid?.let { video.uuid.eq(it) },
                request.title?.let { video.title.containsIgnoreCase(it) },
                request.channelName?.let { video.channel.name.containsIgnoreCase(it) },
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
                request.id?.let { video.id.eq(it) },
                request.uuid?.let { video.uuid.eq(it) },
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
                request.channelName?.let { channel.name.containsIgnoreCase(it) },
                request.channelUuid?.let { channel.uuid.eq(it) }
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
                request.channelName?.let { channel.name.containsIgnoreCase(it) },
                request.channelUuid?.let { channel.uuid.eq(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }

    override fun sumDuration(): Long? {
        return queryFactory.select(video.duration.sum().longValue()).from(video).fetchOne()
    }

    override fun sumFileSize(): Long? {
        return queryFactory.select(video.fileSize.sum()).from(video).fetchOne()
    }

    override fun getDailyVideoStats(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AdminDashboardVideoHistoriesResponse.DailyStat> {
        // 각 날짜별 누적 값 계산
        val result = mutableListOf<AdminDashboardVideoHistoriesResponse.DailyStat>()
        var currentDate = startDate

        while (!currentDate.isAfter(endDate)) {
            val endOfDay = currentDate.plusDays(1).atStartOfDay()

            val videoCount = queryFactory
                    .select(video.count())
                    .from(video)
                    .where(video.createdAt.lt(endOfDay))
                    .fetchOne()
                    ?: 0L

            val storageUsage = queryFactory
                    .select(video.fileSize.sum())
                    .from(video)
                    .where(video.createdAt.lt(endOfDay))
                    .fetchOne()
                    ?: 0L

            result.add(
                AdminDashboardVideoHistoriesResponse.DailyStat(
                    date = currentDate,
                    videoCount = videoCount,
                    storageUsage = storageUsage
                )
            )

            currentDate = currentDate.plusDays(1)
        }

        return result
    }

    override fun countByChannelId(channelId: Long): Long {
        return queryFactory
            .select(video.count())
            .from(video)
            .where(
                video.channelId.eq(channelId),
                video.isActive.eq(true)
            )
            .fetchOne() ?: 0L
    }

    override fun sumFileSizeByChannelId(channelId: Long): Long {
        return queryFactory
            .select(video.fileSize.sum())
            .from(video)
            .where(
                video.channelId.eq(channelId),
                video.isActive.eq(true)
            )
            .fetchOne() ?: 0L
    }
}
