package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.Channel
import org.springframework.data.jpa.repository.JpaRepository

interface ChannelRepository : JpaRepository<Channel, Long>, ChannelRepositoryCustom {
    fun findByUuid(uuid: String): Channel?
}
