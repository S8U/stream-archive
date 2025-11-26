package com.github.s8u.streamarchive.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.nio.file.Path
import java.nio.file.Paths

@Configuration
@ConfigurationProperties(prefix = "storage")
class StorageProperties {
    var basePath: String = "./storage"

    val channelsPath: Path
        get() = Paths.get(basePath, "channels")

    val videosPath: Path
        get() = Paths.get(basePath, "videos")

    fun getChannelPath(channelId: Long): Path {
        return channelsPath.resolve(channelId.toString())
    }

    fun getChannelProfilePath(channelId: Long): Path {
        return getChannelPath(channelId).resolve("profile.png")
    }

    fun getVideoPath(videoId: Long): Path {
        return videosPath.resolve(videoId.toString())
    }

    fun getVideoSegmentsPath(videoId: Long): Path {
        return getVideoPath(videoId).resolve("segments")
    }
}