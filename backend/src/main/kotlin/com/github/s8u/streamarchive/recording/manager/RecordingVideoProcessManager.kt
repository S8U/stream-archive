package com.github.s8u.streamarchive.recording.manager

import com.github.s8u.streamarchive.global.properties.StorageProperties
import com.github.s8u.streamarchive.recording.event.RecordingProcessEndedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class RecordingVideoProcessManager(
    private val eventPublisher: ApplicationEventPublisher,
    private val storageProperties: StorageProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

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
        videoId: Long,
        streamlinkArgs: List<String> = emptyList()
    ) {
        try {
            // 동영상 디렉토리 생성
            val videoPath = storageProperties.getVideoPath(videoId)
            Files.createDirectories(videoPath)

            // 파일 경로 설정
            val playlistPath = storageProperties.getVideoPlaylistPath(videoId)
            val segmentPattern = storageProperties.getVideoSegmentPattern(videoId)

            // Streamlink
            val streamlinkCommand = mutableListOf(
                "streamlink",
                streamUrl,
                quality,
                "-O"
            )
            streamlinkCommand.addAll(streamlinkArgs)

            // FFmpeg
            val ffmpegCommand = listOf(
                "ffmpeg",
                "-y",                           // 덮어쓰기
                "-v", "error",                  // 에러만 로깅
                "-i", "pipe:0",                 // stdin에서 입력
                "-c:v", "copy",                 // 동영상 코덱 복사 (재인코딩 X)
                "-c:a", "copy",                 // 오디오 코덱 복사 (재인코딩 X)
                "-f", "hls",                    // HLS 형식
                "-hls_time", "3",               // 3초 세그먼트
                "-hls_list_size", "0",          // 무제한 세그먼트
                "-hls_segment_filename",
                segmentPattern,
                playlistPath.toString()
            )

            logger.debug("RecordingVideoProcessManager: Streamlink command: {}", streamlinkCommand.joinToString(" "))
            logger.debug("RecordingVideoProcessManager: FFmpeg command: {}", ffmpegCommand.joinToString(" "))
            
            // 녹화 프로세스 시작
            val processBuilders = listOf(
                ProcessBuilder(streamlinkCommand),
                ProcessBuilder(ffmpegCommand)
            )

            val recordProcesses = ProcessBuilder.startPipeline(processBuilders)
            processes[recordId] = recordProcesses

            // 각 프로세스의 stderr를 읽어 로그로 남김 (실패 원인 파악용)
            val processNames = listOf("streamlink", "ffmpeg")
            recordProcesses.forEachIndexed { index, process ->
                val name = processNames.getOrElse(index) { "process$index" }
                Thread {
                    process.errorStream.bufferedReader().useLines { lines ->
                        lines.forEach { line ->
                            logger.warn("[{}] recordId={}: {}", name, recordId, line)
                        }
                    }
                }.apply { isDaemon = true }.start()
            }

            logger.info(
                "RecordingVideoProcessManager: Recording process started: recordId={}, videoId={}, quality={}, streamUrl={}",
                recordId, videoId, quality, streamUrl
            )

            // 백그라운드 스레드에서 프로세스 종료 감지
            Thread {
                try {
                    // 모든 프로세스가 종료될 때까지 대기
                    recordProcesses.forEach { it.waitFor() }

                    val exitCodes = recordProcesses.mapIndexed { index, process ->
                        val name = processNames.getOrElse(index) { "process$index" }
                        "$name=${process.exitValue()}"
                    }.joinToString(", ")
                    logger.info("RecordingVideoProcessManager: Recording process ended: recordId={}, exitCodes=[{}]", recordId, exitCodes)

                    // 프로세스가 스스로 종료된 사실을 발행 (리스너가 녹화 종료 처리)
                    eventPublisher.publishEvent(RecordingProcessEndedEvent(recordId))

                } catch (e: InterruptedException) {
                    logger.warn("RecordingVideoProcessManager: Recording process monitoring interrupted: recordId={}", recordId)
                    Thread.currentThread().interrupt()
                } catch (e: Exception) {
                    logger.error("RecordingVideoProcessManager: Error monitoring recording process: recordId={}", recordId, e)
                } finally {
                    processes.remove(recordId)
                }
            }.start()

        } catch (e: Exception) {
            logger.error("RecordingVideoProcessManager: Failed to start recording process: recordId={}", recordId, e)
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
            logger.warn("RecordingVideoProcessManager: No recording process found to stop: recordId={}", recordId)
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
                        logger.warn("RecordingVideoProcessManager: Forcibly destroyed recording process: recordId={}", recordId)
                    }
                } catch (e: Exception) {
                    logger.error("RecordingVideoProcessManager: Error stopping recording process: recordId={}", recordId, e)
                }
            }
        }

        logger.info("RecordingVideoProcessManager: Recording process stopped: recordId={}", recordId)
    }

}
