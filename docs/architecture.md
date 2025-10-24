# Stream Archive 시스템 아키텍처

## 1. 시스템 개요

Stream Archive는 다중 플랫폼 스트리밍 녹화 시스템으로, 수평 확장이 가능한 마이크로서비스 아키텍처를 기반으로 설계되었습니다.

### 1.1 핵심 원칙
- **수평 확장 가능**: 모든 컴포넌트는 부하에 따라 수평 확장 가능
- **느슨한 결합**: Redis 큐를 통한 비동기 통신
- **고가용성**: 단일 장애점(SPOF) 최소화
- **점진적 분리**: 초기에는 모노리스로 개발, 필요 시 물리적 분리

## 2. 시스템 구성 요소

### 2.1 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                         Frontend                            │
│                  (Next.js + React 19)                       │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP/REST
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    API Server (Backend)                     │
│              (Spring Boot + Kotlin + JPA)                   │
│  - 다시보기 API                                              │
│  - 사용자 인증/인가 (JWT)                                    │
│  - HLS 스트림 서빙                                           │
└────────┬────────────────────────────────────────────────────┘
         │
         ├─────────────────┬─────────────────┬─────────────────┐
         │                 │                 │                 │
         ▼                 ▼                 ▼                 ▼
    ┌────────┐      ┌──────────┐     ┌──────────┐      ┌──────────┐
    │   DB   │      │  Redis   │     │   NFS1   │      │   NFS2   │
    │MariaDB │      │(큐/캐시)  │     │(Storage) │      │(Storage) │
    └────────┘      └──────────┘     └──────────┘      └──────────┘
         ▲                 ▲                 ▲                 ▲
         │                 │                 │                 │
         └─────────┬───────┴─────────────────┴─────────────────┘
                   │
    ┌──────────────┴──────────────────────────────────────────┐
    │                                                          │
    ▼                                    ▼                     ▼
┌─────────────────┐         ┌─────────────────┐   ┌─────────────────┐
│ Detection       │  Redis  │ Recorder        │   │ Recorder        │
│ Server          │  Queue  │ Server 1        │   │ Server 2        │
│                 ├────────►│                 │   │                 │
│ - 방송 감지      │         │ - 녹화 수행     │   │ - 녹화 수행     │
│ - 큐에 푸시      │         │ - 메타데이터    │   │ - 메타데이터    │
└─────────────────┘         │   수집          │   │   수집          │
                            └─────────────────┘   └─────────────────┘
```

### 2.2 컴포넌트 상세

#### 2.2.1 Database (MariaDB)
**역할:**
- 모든 메타데이터 저장
- 채널, 비디오, 스케줄, 사용자 정보 관리

**수평 확장:**
- Read Replica를 통한 읽기 확장
- Write는 Primary 단일 노드 (추후 Clustering 고려)

#### 2.2.2 Redis
**역할:**
- **녹화 큐**: 감지된 스트림을 녹화 서버로 전달
- **헬스체크**: 녹화 서버 및 스토리지 서버 상태 관리

**데이터 구조:**
```
# 녹화 큐
LIST recording:queue -> [{streamInfo}, {streamInfo}, ...]

# 녹화 서버 헬스체크
KEY recorder:server1:health = "alive" (TTL: 10s)
KEY recorder:server1:count = 3

# 스토리지 헬스체크
KEY storage:1:healthy = "true" (TTL: 60s)
KEY storage:1:latency = "15"
```

#### 2.2.3 파일 스토리지 (NFS)
**역할:**
- HLS 녹화 파일 저장
- 여러 스토리지 서버 연결 가능

**DB 테이블:**
```sql
CREATE TABLE storage_servers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    mount_path VARCHAR(255) NOT NULL,     -- /nfs1, /nfs2
    base_url VARCHAR(255),                 -- http://storage1.example.com
    total_capacity BIGINT,
    used_capacity BIGINT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    priority INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- videos 테이블에 FK 추가
ALTER TABLE videos ADD COLUMN storage_server_id BIGINT;
```

**헬스체크:**
- 녹화 서버가 ShedLock을 이용해 30초마다 체크
- 쓰기 테스트 수행 후 Redis에 상태 저장

#### 2.2.4 스트리밍 감지 서버 (Detection Server)
**역할:**
- 등록된 채널의 방송 시작/종료 감지
- 스케줄 매칭 확인
- Redis 큐에 녹화 요청 푸시

**동작 방식:**
```kotlin
@Scheduled(fixedRate = 10000) // 10초마다
fun detectStreams() {
    channelPlatformRepository.findAll().forEach { channelPlatform ->
        val streamInfo = platformAdapter.getStreamInfo(channelPlatform)

        if (streamInfo.isLive) {
            // 중복 체크
            if (!isAlreadyRecording(streamInfo.platformStreamId)) {
                // 스케줄 매칭
                if (shouldRecord(channelPlatform, streamInfo)) {
                    redisQueue.push("recording:queue", RecordingRequest(
                        channelId = channelPlatform.channelId,
                        platformType = channelPlatform.platformType,
                        streamInfo = streamInfo
                    ))
                }
            }
        }
    }
}
```

**수평 확장:**
- 초기: 1대로 시작 (단순)
- 필요 시: 여러 대 + 분산락으로 중복 감지 방지

#### 2.2.5 녹화 서버 (Recorder Server)
**역할:**
- Redis 큐에서 녹화 요청 소비
- streamlink + ffmpeg 파이프라인으로 녹화
- 실시간 메타데이터 수집 (채팅, 시청자 수 등)
- 스토리지 헬스체크

**동작 방식:**

1. **큐 소비 (부하 기반)**
```kotlin
@Scheduled(fixedRate = 1000)
fun consumeRecordingQueue() {
    val currentLoad = getCurrentRecordingCount()
    val maxLoad = getMaxConcurrentRecordings()

    if (currentLoad < maxLoad) {
        val request = redisQueue.pop("recording:queue")
        if (request != null) {
            startRecording(request)
        }
    }
}
```

2. **헬스체크 보고**
```kotlin
@Scheduled(fixedRate = 5000)
fun reportHealth() {
    redis.setex("recorder:${serverId}:health", 10, "alive")
    redis.setex("recorder:${serverId}:count", 10, getCurrentRecordingCount().toString())
}
```

3. **스토리지 헬스체크 (ShedLock)**
```kotlin
@SchedulerLock(name = "storage-health-check", lockAtMostFor = "25s")
@Scheduled(fixedRate = 30000)
fun checkStorageHealth() {
    storageServerRepository.findAll().forEach { storage ->
        try {
            val healthFile = "${storage.mountPath}/.health_check"
            File(healthFile).writeText(System.currentTimeMillis().toString())
            File(healthFile).delete()
            redis.setex("storage:${storage.id}:healthy", 60, "true")
        } catch (e: Exception) {
            redis.setex("storage:${storage.id}:healthy", 60, "false")
        }
    }
}
```

4. **녹화 시작**
```kotlin
fun startRecording(request: RecordingRequest) {
    // 1. Healthy한 스토리지 선택
    val storage = selectHealthyStorage()

    // 2. 쓰기 가능 확인
    val recordDir = "${storage.mountPath}/${video.uuid}"
    Files.createDirectories(Paths.get(recordDir))

    // 3. 녹화 프로세스 시작
    val processes = ProcessBuilder.startPipeline(listOf(
        ProcessBuilder("streamlink", streamUrl, quality, "-O"),
        ProcessBuilder("ffmpeg", "-i", "pipe:0", "-c", "copy",
                       "-f", "hls", "-hls_time", "3",
                       playlistPath)
    ))

    // 4. 메타데이터 수집 시작
    startMetadataCollection(video)
}
```

**수평 확장:**
- 녹화 부하에 따라 서버 추가
- Redis 큐를 통해 자동으로 작업 분배
- 각 서버가 자신의 부하를 Redis에 보고

#### 2.2.6 API 서버 (Backend)
**역할:**
- 다시보기 API 제공
- 사용자 인증/인가 (JWT)
- HLS 스트림 서빙
- 관리자 기능

**수평 확장:**
- Stateless 서버로 설계 (JWT 기반)
- 로드 밸런서를 통한 트래픽 분산
- 세션 정보 없음 (Redis 캐시 공유)

#### 2.2.7 Frontend
**역할:**
- 사용자 인터페이스
- HLS 비디오 플레이어
- 실시간 채팅 리플레이

**기술 스택:**
- Next.js 15 (App Router)
- React 19
- Tailwind CSS 4
- HLS.js (비디오 플레이어)

## 3. 데이터 플로우

### 3.1 녹화 플로우

```
1. Detection Server
   │
   ├─ 10초마다 플랫폼 API 폴링
   │
   ├─ 방송 시작 감지
   │
   ├─ 스케줄 매칭 확인
   │
   └─ Redis 큐에 푸시
       │
       ▼
2. Redis Queue
   │
   │ (FIFO)
   │
   ▼
3. Recorder Server (여러 대)
   │
   ├─ 1초마다 큐 체크
   │
   ├─ 현재 부하 < Max일 때만 소비
   │
   ├─ Healthy한 스토리지 선택
   │
   ├─ streamlink + ffmpeg 시작
   │
   ├─ DB에 Record 생성
   │
   └─ 메타데이터 수집 시작
       │
       ├─ WebSocket으로 채팅 수집
       │
       └─ 30초마다 API로 시청자/제목/카테고리 수집
```

### 3.2 다시보기 플로우

```
1. Frontend
   │
   ├─ 비디오 페이지 접근
   │
   └─ GET /api/videos/{uuid}
       │
       ▼
2. API Server
   │
   ├─ 권한 확인 (JWT)
   │
   ├─ DB에서 비디오 메타데이터 조회
   │
   ├─ storage_server_id로 스토리지 경로 확인
   │
   └─ HLS 플레이리스트 경로 반환
       │
       ▼
3. Frontend
   │
   ├─ HLS.js로 플레이어 초기화
   │
   ├─ GET /api/videos/{uuid}/playlist.m3u8
   │
   └─ 세그먼트 파일 스트리밍
       │
       ▼
4. API Server
   │
   ├─ NFS에서 파일 읽기
   │
   └─ 클라이언트에 스트림
```

## 4. 개발 및 배포 전략

### 4.1 Phase 1: 모노리스 개발 (현재)

**프로젝트 구조:**
```
backend/
├── src/main/kotlin/com/github/s8u/streamarchive/
│   ├── common/           # 공통 (Entity, Repository, Utils)
│   ├── detection/        # 스트리밍 감지
│   │   ├── DetectionScheduler.kt
│   │   └── PlatformAdapter.kt
│   ├── recorder/         # 녹화 서버
│   │   ├── RecordingService.kt
│   │   ├── MetadataCollector.kt
│   │   └── StorageHealthCheck.kt
│   ├── api/             # 다시보기 API
│   │   ├── controller/
│   │   └── service/
│   └── StreamArchiveApplication.kt
└── application.yml
```

### 4.2 Phase 2: 물리적 분리

**프로젝트 구조:**
```
stream-archive/
├── common/                    # 공통 라이브러리
│   └── build.gradle.kts
├── detection-server/
│   ├── src/
│   └── build.gradle.kts
├── recorder-server/
│   ├── src/
│   └── build.gradle.kts
└── api-server/
    ├── src/
    └── build.gradle.kts
```

**장점:**
- 완전 독립적 배포
- 각 서버별 최적화 가능
- 의존성 명확

**단점:**
- 복잡도 증가
- 공통 코드 관리 필요