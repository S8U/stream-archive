# 플랫폼 공통 녹화 시스템 기능 명세서

## 1. 시스템 개요

### 1.1 목적
다중 스트리밍 플랫폼(Chzzk, Twitch, SOOP, YouTube)에서 방송을 자동으로 감지하고 녹화하는 통합 시스템

### 1.2 핵심 아키텍처
```
방송 감지 → streamlink → pipe → ffmpeg → HLS 파일 생성
         ↓
    실시간 메타데이터 수집 (WebSocket + API)
```

## 2. 핵심 기능 명세

### 2.1 방송 감지 시스템

#### 2.1.1 플랫폼별 모니터링
- **지원 플랫폼**: Chzzk, Twitch, SOOP, YouTube
- **감지 방식**: 각 플랫폼 API 주기적 폴링
- **폴링 주기**: 30초 간격 (설정 가능)
- **감지 대상**: 등록된 채널의 방송 시작/종료 상태

#### 2.1.2 방송 정보 추적
각 플랫폼에서 조회한 방송 정보를 통일된 형태로 표현하는 공통 데이터 클래스:

```kotlin
data class StreamInfo {
    val platformType: PlatformType        // 플랫폼 구분 (CHZZK, TWITCH, SOOP, YOUTUBE)
    val platformChannelId: String         // 플랫폼별 채널 ID
    val platformStreamId: String?         // 방송 고유 ID (라이브 중일 때만)
    val isLive: Boolean                   // 현재 라이브 상태
    val title: String?                    // 방송 제목
    val category: String?                 // 카테고리/게임명
    val viewerCount: Int?                 // 현재 시청자 수
    val thumbnailUrl: String?             // 썸네일 URL
    val startedAt: LocalDateTime?         // 방송 시작 시간
}
```

각 플랫폼 어댑터는 플랫폼별 API 응답을 이 공통 모델로 변환합니다.

### 2.2 녹화 스케줄링 시스템

#### 2.2.1 스케줄 유형
- **ONCE**: 특정 날짜/시간에 1회 녹화
- **ALWAYS**: 방송 시작 시마다 자동 녹화
- **N_DAYS_OF_EVERY_WEEK**: 지정된 요일에만 녹화
- **SPECIFIC_DAY**: 사용자가 지정한 특정 날짜들에만 녹화

#### 2.2.2 스케줄 매칭 로직
```kotlin
fun shouldRecord(
    schedule: RecordSchedule,
    currentTime: LocalDateTime,
    streamInfo: StreamInfo
): Boolean {
    return when (schedule.type) {
        ONCE -> isOnceScheduleMatch(schedule, currentTime)
        ALWAYS -> true
        N_DAYS_OF_EVERY_WEEK -> isWeeklyScheduleMatch(schedule, currentTime)
        SPECIFIC_DAY -> isSpecificDayMatch(schedule, currentTime)
    }
}
```

### 2.3 중복 녹화 방지 시스템

#### 2.3.1 중복 체크 메커니즘
- **기준**: `platform_stream_id` (플랫폼별 방송 고유 ID)
- **체크 시점**: 녹화 시작 전
- **동작**: 동일한 `platform_stream_id`가 이미 녹화 중이면 새 녹화 시작 안함

#### 2.3.2 중복 체크 쿼리
```sql
SELECT COUNT(*) FROM records
WHERE platform_stream_id = ?
  AND is_ended = false
  AND is_cancelled = false
```

### 2.4 녹화 파이프라인

#### 2.4.1 녹화 프로세스
```kotlin
// Kotlin ProcessBuilder를 통한 파이프라인 구성
val qualityFallbackString = record.recordQuality.buildFallbackString()

val processes = ProcessBuilder.startPipeline(listOf(
    ProcessBuilder(
        "streamlink",
        platformAdapter.getStreamUrl(channelPlatform.platformUsername),
        qualityFallbackString,  // 예: "1080p60,1080p,720p60,720p,480p,240p,144p,worst"
        "-O"  // stdout으로 출력
    ),
    ProcessBuilder(
        "ffmpeg",
        "-i", "pipe:0",           // stdin에서 입력 받음
        "-c:v", "copy",           // 비디오 코덱 복사 (재인코딩 없음)
        "-c:a", "copy",           // 오디오 코덱 복사 (재인코딩 없음)
        "-f", "hls",              // HLS 형식으로 출력
        "-hls_time", "3",         // 3초 단위 세그먼트
        "-hls_list_size", "0",    // 무제한 세그먼트 유지
        "-hls_segment_filename", "${segmentPath}/%d.ts",
        playlistPath.toString()   // 플레이리스트 파일 경로
    )
))

recordProcessManager.registerProcesses(record.id, processes[0], processes[1])
```

#### 2.4.2 파일 구조
```
/recordings/
  /{video_uuid}/
    playlist.m3u8       # HLS 플레이리스트
    segment_000.ts      # 비디오 세그먼트들
    segment_001.ts
    ...
    metadata.json       # 녹화 메타데이터
```

#### 2.4.3 품질 설정
품질은 다음 순서로 정의되며, 지원하지 않는 품질일 경우 바로 아래 단계로 fallback:

1. **best**: 최고 품질 자동 선택
2. **2160p60**: 4K 60fps
3. **2160p**: 4K 30fps
4. **1440p60**: 1440p 60fps
5. **1440p**: 1440p 30fps
6. **1080p60**: 1080p 60fps
7. **1080p**: 1080p 30fps
8. **720p60**: 720p 60fps
9. **720p**: 720p 30fps
10. **480p**: 480p
11. **240p**: 240p
12. **144p**: 144p
13. **worst**: 최저 품질 자동 선택

**Streamlink Fallback 구현**:
streamlink의 콤마 구분 품질 목록을 활용하여 자동 fallback 처리:

```bash
# 1080p60 요청 시 생성되는 fallback 문자열:
streamlink [URL] "1080p60,1080p,720p60,720p,480p,240p,144p,worst"
```

```kotlin
enum class RecordQuality(val streamlinkValue: String) {
    BEST("best"),
    P2160_60("2160p60"),
    P2160("2160p"),
    P1440_60("1440p60"),
    P1440("1440p"),
    P1080_60("1080p60"),
    P1080("1080p"),
    P720_60("720p60"),
    P720("720p"),
    P480("480p"),
    P240("240p"),
    P144("144p"),
    WORST("worst");

    fun buildFallbackString(): String {
        return values().drop(ordinal).joinToString(",") { it.streamlinkValue }
    }
}
```

예시:
- `1080p60` → `"1080p60,1080p,720p60,720p,480p,240p,144p,worst"`
- `144p` → `"144p,worst"`

### 2.5 실시간 메타데이터 수집

#### 2.5.1 수집 데이터 유형
1. **채팅 메시지**
    - 수집 방식: WebSocket 연결
    - 저장 정보: 사용자명, 메시지, 추가 데이터(뱃지, 이모트 등)
    - 타임스탬프: 녹화 시작 시점 기준 오프셋(밀리초)

2. **시청자 수**
    - 수집 방식: API 폴링 (30초 간격)
    - 저장 정보: 시청자 수
    - 타임스탬프: 녹화 시작 시점 기준 오프셋(밀리초)

3. **방송 제목**
    - 수집 방식: API 폴링 (30초 간격)
    - 저장 정보: 변경된 제목
    - 트리거: 이전 제목과 다를 때만 저장

4. **방송 카테고리**
    - 수집 방식: API 폴링 (30초 간격)
    - 저장 정보: 변경된 카테고리/게임명
    - 트리거: 이전 카테고리와 다를 때만 저장

#### 2.5.2 타임스탬프 동기화
```kotlin
class TimestampManager {
    private val recordStartTime: Long = System.currentTimeMillis()

    fun getCurrentOffset(): Long {
        return System.currentTimeMillis() - recordStartTime
    }
}
```

### 2.6 녹화 상태 관리

#### 2.6.1 녹화 상태 정의
```kotlin
enum class RecordStatus {
    WAITING,    // 녹화 대기 중
    RECORDING,  // 녹화 진행 중
    COMPLETED,  // 녹화 완료
    FAILED,     // 녹화 실패
    CANCELLED   // 녹화 취소
}
```

#### 2.6.2 상태 전이도
```
WAITING → RECORDING → COMPLETED
   ↓           ↓
CANCELLED  → FAILED
```

### 2.7 실시간 시청 지원

#### 2.7.1 HLS 스트리밍
- **형식**: HTTP Live Streaming (HLS)
- **세그먼트 길이**: 3초 (실시간 시청 최적화)
- **플레이리스트 업데이트**: 실시간
- **지연시간**: 약 3초 (세그먼트 길이)

#### 2.7.2 웹 플레이어 통합
```javascript
// HLS.js를 사용한 실시간 스트림 재생
const video = document.getElementById('video');
const hls = new Hls();
hls.loadSource('/api/recordings/{videoId}/live.m3u8');
hls.attachMedia(video);
```

## 3. 플랫폼별 구현 사항

### 3.1 Chzzk (치지직)
- **API**: Naver Chzzk API
- **채팅**: WebSocket 연결
- **인증**: 쿠키 기반

### 3.2 Twitch
- **API**: Twitch Helix API
- **채팅**: IRC/WebSocket
- **인증**: OAuth 2.0

### 3.3 SOOP (아프리카TV)
- **API**: AfreecaTV API
- **채팅**: WebSocket 연결
- **인증**: API 키

### 3.4 YouTube
- **API**: YouTube Data API v3
- **채팅**: YouTube Live Chat API
- **인증**: OAuth 2.0

## 4. 녹화 종료 처리

### 4.1 프로세스 종료 감지
- **감지 방법**: ffmpeg 프로세스 종료 모니터링
- **처리**: DB에서 녹화 상태를 COMPLETED로 변경
- **후처리**: 메타데이터 저장 및 파일 정보 업데이트

## 5. 설정 관리 시스템

### 5.1 설정 구조
시스템은 2단계 설정 구조를 가집니다:
- **글로벌 설정**: 시스템 전체 기본값
- **채널별 설정**: 채널별 개별 설정 (글로벌 설정 override)

### 5.2 설정 fallback 로직
```kotlin
fun getChannelSetting(channelId: Long, key: String): String {
    return channelSettings[channelId][key]
        ?: globalSettings[key]
        ?: defaultValue
}
```

### 5.3 주요 설정 항목

#### 5.3.1 글로벌 설정
- `default_retention_period_days`: 기본 보존 기간 (일)
- `max_concurrent_recordings`: 최대 동시 녹화 수
- `polling_interval_seconds`: API 폴링 주기
- `default_record_quality`: 기본 녹화 품질

#### 5.3.2 채널별 설정
- `retention_period_days`: 채널별 보존 기간
- `record_quality`: 채널별 기본 녹화 품질
- `is_auto_cleanup_enabled`: 자동 정리 활성화 여부

## 6. 성능 및 확장성

### 6.1 동시 녹화 제한
- **최대 동시 녹화**: 10개 (하드웨어 성능에 따라 조정)
- **우선순위**: 먼저 시작된 녹화 우선

### 6.2 리소스 모니터링
- **CPU 사용량**: 80% 초과 시 새 녹화 제한
- **메모리 사용량**: 90% 초과 시 새 녹화 제한
- **네트워크 대역폭**: 모니터링 및 알림

### 6.3 스토리지 관리
- **자동 정리**: 채널별로 설정된 보존 기간 후 자동 삭제
- **압축**: 완료된 녹화의 HLS → MP4 변환 (선택사항)
- **채널별 보존 정책**: 각 채널마다 독립적인 보존 기간 설정 가능

## 7. 시청 기록 시스템

### 7.1 시청 기록 저장

#### 7.1.1 데이터 구조
```kotlin
data class VideoWatchHistory(
    val id: Long,
    val userId: Long,
    val videoId: Long,
    val lastPosition: Int,        // 마지막 재생 위치 (초)
    val watchedAt: LocalDateTime  // 마지막 시청 시각
)
```

#### 7.1.2 저장 메커니즘
- **저장 주기**: 5-10초마다 현재 재생 위치 전송
- **UPSERT 방식**: 동일 사용자-영상 조합은 업데이트, 신규는 생성
- **페이지 이탈 시**: `beforeunload` 이벤트로 마지막 위치 저장
- **고유 제약**: 한 사용자당 한 영상에 하나의 기록만 유지

#### 7.1.3 API 엔드포인트
```kotlin
// POST /api/videos/{uuid}/watch-history
fun saveWatchHistory(
    userId: Long,
    videoUuid: String,
    request: SaveWatchHistoryRequest
) {
    val video = videoRepository.findByUuid(videoUuid)
    val existing = watchHistoryRepository.findByUserIdAndVideoId(userId, video.id)

    if (existing != null) {
        existing.lastPosition = request.position
        existing.watchedAt = LocalDateTime.now()
    } else {
        watchHistoryRepository.save(VideoWatchHistory(
            userId = userId,
            videoId = video.id,
            lastPosition = request.position,
            watchedAt = LocalDateTime.now()
        ))
    }
}
```

### 7.2 이어보기 기능

#### 7.2.1 재생 위치 복원
- 영상 페이지 로드 시 시청 기록 조회
- 기록이 있으면 마지막 재생 위치로 이동
- "처음부터 보기" / "이어보기" 선택지 제공

#### 7.2.2 진행률 계산
```kotlin
progress = (lastPosition / video.duration) * 100
```

#### 7.2.3 완료 처리
- 90% 이상 시청 시 진행률 바를 완료로 표시
- 재생 위치가 끝에서 10초 이내면 완료로 간주

### 7.3 시청 기록 조회

#### 7.3.1 목록 조회 API
```kotlin
// GET /api/history?page=0&size=20
fun getWatchHistories(
    userId: Long,
    pageable: Pageable
): Page<WatchHistoryResponse> {
    return watchHistoryRepository
        .findByUserIdOrderByWatchedAtDesc(userId, pageable)
        .map { history ->
            val video = videoRepository.findById(history.videoId)
            WatchHistoryResponse(
                videoUuid = video.uuid,
                title = video.title,
                channelName = channelRepository.findById(video.channelId).name,
                thumbnailUrl = generateThumbnailUrl(video),
                lastPosition = history.lastPosition,
                duration = video.duration,
                progress = (history.lastPosition.toDouble() / video.duration * 100).toInt(),
                watchedAt = history.watchedAt
            )
        }
}
```

#### 7.3.2 개별 영상 기록 조회
```kotlin
// GET /api/videos/{uuid}/watch-history
fun getWatchHistory(
    userId: Long,
    videoUuid: String
): WatchHistoryDto? {
    val video = videoRepository.findByUuid(videoUuid)
    return watchHistoryRepository.findByUserIdAndVideoId(userId, video.id)
        ?.let { WatchHistoryDto(lastPosition = it.lastPosition) }
}
```

### 7.4 시청 기록 삭제

#### 7.4.1 개별 삭제
```kotlin
// DELETE /api/history/{videoUuid}
fun deleteWatchHistory(userId: Long, videoUuid: String) {
    val video = videoRepository.findByUuid(videoUuid)
    watchHistoryRepository.deleteByUserIdAndVideoId(userId, video.id)
}
```

#### 7.4.2 전체 삭제
```kotlin
// DELETE /api/history
fun deleteAllWatchHistories(userId: Long) {
    watchHistoryRepository.deleteAllByUserId(userId)
}
```

### 7.5 프라이버시 및 보안
- **사용자별 격리**: 각 사용자는 자신의 시청 기록만 조회 가능
- **관리자 제한**: 관리자도 다른 사용자의 시청 기록 접근 불가
- **CASCADE 삭제**: 사용자 삭제 시 모든 시청 기록 자동 삭제
- **영상 삭제 시**: 해당 영상의 모든 시청 기록 자동 삭제

### 7.6 성능 최적화
- **배치 전송**: 5-10초 간격으로 재생 위치 업데이트
- **인덱스**: `(user_id, watched_at)` 복합 인덱스로 목록 조회 최적화
- **페이징**: 시청 기록 목록은 페이징 처리로 대량 데이터 대응