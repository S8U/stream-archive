package com.github.s8u.streamarchive.repository

import com.github.s8u.streamarchive.entity.ChannelPlatform
import org.springframework.data.jpa.repository.JpaRepository

interface ChannelPlatformRepository : JpaRepository<ChannelPlatform, Long> {
}