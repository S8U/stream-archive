package com.github.s8u.streamarchive.detect.usecase

import com.github.s8u.streamarchive.channelplatform.repository.ChannelPlatformRepository
import com.github.s8u.streamarchive.detect.event.StreamDetectedEvent
import com.github.s8u.streamarchive.platform.strategy.PlatformStrategyFactory
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 스트리밍 감지
 *
 * 등록된 모든 채널 플랫폼을 폴링해 방송 중이면 감지 이벤트를 발행한다.
 */
@Service
class StreamDetectUseCase(
    private val channelPlatformRepository: ChannelPlatformRepository,
    private val platformStrategyFactory: PlatformStrategyFactory,
    private val eventPublisher: ApplicationEventPublisher
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun detect() {
        val channelPlatforms = channelPlatformRepository.findAll()

        channelPlatforms.forEach { channelPlatform ->
            // 채널 플랫폼 한 개 폴링 (호출이 실패하면 이 채널은 건너뛴다)
            try {
                val strategy = platformStrategyFactory.getPlatformStrategy(channelPlatform.platformType)
                val stream = strategy.getStream(channelPlatform.platformChannelId)

                if (stream != null) {
                    // 이벤트는 엔티티 대신 식별자만 담는다 (리스너가 트랜잭션 밖에서 받으므로)
                    val channelId = channelPlatform.channel?.id ?: return@forEach

                    logger.debug(
                        "StreamDetectUseCase: Stream detected: channelId={}, platformType={}, streamId={}",
                        channelId,
                        channelPlatform.platformType,
                        stream.id
                    )
                    eventPublisher.publishEvent(
                        StreamDetectedEvent(
                            channelPlatformId = channelPlatform.id!!,
                            channelId = channelId,
                            platformType = channelPlatform.platformType,
                            stream = stream
                        )
                    )
                }
            } catch (e: Exception) {
                logger.error(
                    "StreamDetectUseCase: Failed to detect stream: channelId={}, platformType={}",
                    channelPlatform.channel?.id,
                    channelPlatform.platformType,
                    e
                )
            }
        }
    }
}
