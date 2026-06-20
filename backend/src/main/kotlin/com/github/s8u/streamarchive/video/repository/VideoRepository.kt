package com.github.s8u.streamarchive.video.repository

import com.github.s8u.streamarchive.video.entity.Video
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface VideoRepository : JpaRepository<Video, Long>, VideoRepositoryCustom {

    fun findByChannelId(channelId: Long): List<Video>
    fun findByUuid(uuid: String): Video?
    fun existsByChannelIdAndIsArchivedTrue(channelId: Long): Boolean

    @Query("SELECT v FROM Video v JOIN FETCH v.channel WHERE v.id IN :videoIds")
    fun findAllByIdInWithChannel(@Param("videoIds") videoIds: List<Long>): List<Video>

}
