package com.github.s8u.streamarchive.channel.repository

import com.github.s8u.streamarchive.channel.entity.Channel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChannelRepository : JpaRepository<Channel, Long>, ChannelRepositoryCustom {

    fun findByUuid(uuid: String): Channel?

    @Query("SELECT c.id FROM Channel c")
    fun findAllIds(): List<Long>

}
