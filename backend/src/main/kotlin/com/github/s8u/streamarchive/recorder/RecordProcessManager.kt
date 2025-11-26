package com.github.s8u.streamarchive.recorder

import com.github.s8u.streamarchive.properties.StorageProperties
import com.github.s8u.streamarchive.service.RecordService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class RecordProcessManager(
    @Lazy private val recordService: RecordService,
    private val storageProperties: StorageProperties
) {
    private val logger = LoggerFactory.getLogger(RecordProcessManager::class.java)

    private val processes = ConcurrentHashMap<Long, List<Process>>() // <RecordId, List<Process>>

    /**
     * 현재 녹화 중인 모든 recordId 조회
     */
    fun getActiveRecordIds(): List<Long> {
        return processes.keys.toList()
    }

    /**
     * 프로세스 실행 여부 확인
     */
    fun isProcessAlive(recordId: Long): Boolean {
        return processes[recordId]?.any { it.isAlive } ?: false
    }

    /**
     * 녹화 프로세스 시작
     */
    fun startRecording(
        recordId: Long,
        streamUrl: String,
        quality: String,
        videoUuid: String,
        platformHeaders: Map<String, String> = emptyMap()
    ) {
        try {
            // 동영상 디렉토리 생성
            val videoPath = storageProperties.videosPath.resolve(videoUuid)
            Files.createDirectories(videoPath)

            // 파일 경로 설정
            val playlistPath = videoPath.resolve("playlist.m3u8")
            val segmentPattern = videoPath.resolve("segment_%d.ts").toString()

            // Streamlink 프로세스 빌더
            val streamlinkArgs = mutableListOf("streamlink", streamUrl, quality, "-O")
            platformHeaders.forEach { (key, value) ->
                streamlinkArgs.add("--http-header")
                streamlinkArgs.add("$key=$value")
            }

            // FFmpeg 프로세스 빌더
            val processBuilders = listOf(
                ProcessBuilder(streamlinkArgs),
                ProcessBuilder(
                    "ffmpeg",
                    "-y",                           // 덮어쓰기
                    "-v", "error",                  // 에러만 로깅
                    "-i", "pipe:0",                 // stdin에서 입력
                    "-c", "copy",                   // 코덱 복사 (재인코딩 X)
                    "-f", "hls",                    // HLS 형식
                    "-hls_time", "3",               // 3초 세그먼트
                    "-hls_list_size", "0",          // 무제한 세그먼트
                    "-hls_segment_filename",
                    segmentPattern,
                    playlistPath.toString()
                )
            )

            // 파이프라인 시작
            val recordProcesses = ProcessBuilder.startPipeline(processBuilders)
            processes[recordId] = recordProcesses

            logger.info(
                "Recording process started: recordId={}, videoUuid={}, quality={}, streamUrl={}",
                recordId, videoUuid, quality, streamUrl
            )

            // 백그라운드 스레드에서 프로세스 종료 감지
            Thread {
                try {
                    // 모든 프로세스가 종료될 때까지 대기
                    recordProcesses.forEach { it.waitFor() }

                    logger.info("Recording process ended: recordId={}", recordId)

                    // 프로세스 종료 시 자동으로 녹화 종료 처리
                    recordService.endRecording(recordId, isCancel = false)

                } catch (e: InterruptedException) {
                    logger.warn("Recording process monitoring interrupted: recordId={}", recordId)
                    Thread.currentThread().interrupt()
                } catch (e: Exception) {
                    logger.error("Error monitoring recording process: recordId={}", recordId, e)
                } finally {
                    processes.remove(recordId)
                }
            }.start()

        } catch (e: Exception) {
            logger.error("Failed to start recording process: recordId={}", recordId, e)
            processes.remove(recordId)
            throw e
        }
    }

    /**
     * 녹화 프로세스 강제 종료
     */
    fun stopRecording(recordId: Long) {
        val recordProcesses = processes.remove(recordId)

        if (recordProcesses == null) {
            logger.warn("No recording process found to stop: recordId={}", recordId)
            return
        }

        recordProcesses.forEach { process ->
            if (process.isAlive) {
                try {
                    // 정상 종료 시도
                    process.destroy()

                    // 5초 대기
                    if (!process.waitFor(5, TimeUnit.SECONDS)) {
                        // 여전히 살아있으면 강제 종료
                        process.destroyForcibly()
                        logger.warn("Forcibly destroyed recording process: recordId={}", recordId)
                    }
                } catch (e: Exception) {
                    logger.error("Error stopping recording process: recordId={}", recordId, e)
                }
            }
        }

        logger.info("Recording process stopped: recordId={}", recordId)
    }

}
