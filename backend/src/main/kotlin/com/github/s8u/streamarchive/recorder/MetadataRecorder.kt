package com.github.s8u.streamarchive.recorder

interface MetadataRecorder {

    suspend fun start()
    suspend fun stop()
    fun isActive(): Boolean

}