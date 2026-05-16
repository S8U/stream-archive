package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.dto.AdminDashboardVideoHistoriesResponse
import com.github.s8u.streamarchive.dto.AdminVideoSearchRequest
import com.github.s8u.streamarchive.dto.PublicVideoSearchRequest
import com.github.s8u.streamarchive.entity.QChannel
import com.github.s8u.streamarchive.entity.QRecord
import com.github.s8u.streamarchive.entity.QVideo
import com.github.s8u.streamarchive.entity.Video
import com.github.s8u.streamarchive.enums.ContentPrivacy
import com.querydsl.core.types.dsl.BooleanExpression
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
                videoTextContains(request.title, request.description),
                request.channelName?.let { video.channel.name.containsIgnoreCase(it) },
                request.contentPrivacy?.let { video.contentPrivacy.eq(it) },
                request.isArchived?.let { video.isArchived.eq(it) },
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
                videoTextContains(request.title, request.description),
                request.channelName?.let { channel.name.containsIgnoreCase(it) },
                request.contentPrivacy?.let { video.contentPrivacy.eq(it) },
                request.isArchived?.let { video.isArchived.eq(it) },
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
                if (request.channelUuid == null) channel.contentPrivacy.eq(ContentPrivacy.PUBLIC) else null,
                video.isActive.eq(true),
                videoTextContains(request.title, request.description),
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
                if (request.channelUuid == null) channel.contentPrivacy.eq(ContentPrivacy.PUBLIC) else null,
                video.isActive.eq(true),
                videoTextContains(request.title, request.description),
                request.channelName?.let { channel.name.containsIgnoreCase(it) },
                request.channelUuid?.let { channel.uuid.eq(it) }
            )
            .fetchOne() ?: 0L

        return PageImpl(results, pageable, total)
    }

    private fun videoTextContains(title: String?, description: String?): BooleanExpression? {
        val titleExpression = title?.let { video.title.containsIgnoreCase(it) }
        val descriptionExpression = description?.let { video.description.containsIgnoreCase(it) }

        return when {
            titleExpression != null && descriptionExpression != null -> titleExpression.or(descriptionExpression)
            titleExpression != null -> titleExpression
            else -> descriptionExpression
        }
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
                video.contentPrivacy.eq(ContentPrivacy.PUBLIC),
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
                video.contentPrivacy.eq(ContentPrivacy.PUBLIC),
                video.isActive.eq(true)
            )
            .fetchOne() ?: 0L
    }

    override fun sumFileSizeByChannelIds(channelIds: Collection<Long>): Map<Long, Long> {
        if (channelIds.isEmpty()) {
            return emptyMap()
        }

        val totalFileSize = video.fileSize.sum()

        return queryFactory
            .select(video.channelId, totalFileSize)
            .from(video)
            .where(
                video.channelId.`in`(channelIds),
                video.isActive.eq(true)
            )
            .groupBy(video.channelId)
            .fetch()
            .associate { tuple ->
                tuple.get(video.channelId)!! to (tuple.get(totalFileSize) ?: 0L)
            }
    }
}
