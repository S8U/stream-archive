# Stream Archive

여러 플랫폼의 스트리밍을 자동으로 감지하고 녹화하여 다시보기를 제공하는 개인 아카이브 시스템.

## 지원 플랫폼

- 치지직 (Chzzk)
- 트위치 (Twitch)
- ~~SOOP (아프리카TV)~~

## 주요 기능

- **스트리밍 감지** — 등록된 채널의 방송 시작을 자동 감지 (API Polling, 10초 주기)
- **스트리밍 녹화** — Streamlink → FFmpeg 파이프라인으로 HLS 녹화 (3초 세그먼트)
- **실시간 시청** — 녹화 중인 방송을 HLS로 실시간 시청 가능 (~3초 딜레이)
- **메타데이터 수집** — 채팅 (WebSocket), 시청자 수·제목·카테고리 변경 이력, 썸네일
- **다시보기** — HLS 플레이어 + 채팅 리플레이 (싱크 지원)
- **녹화 스케줄** — 항상 / 한 번만 / 매주 N요일 / 날짜 지정
- **채널 관리** — 멀티 플랫폼 연동, 공개 범위 설정 (공개/비공개/일부 공개)
- **시청 기록** — 이어보기 지원
- **관리자 대시보드** — 녹화 현황, 시스템 상태, 채널/스케줄/비디오/사용자 관리

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Spring Boot 3.5, Kotlin 1.9, JPA/Hibernate, QueryDSL, MariaDB |
| Frontend | Next.js 15, React 19, TypeScript, Tailwind CSS 4, Radix UI |
| 녹화 | Streamlink, FFmpeg (HLS) |
| 상태 관리 | Zustand, TanStack React Query |
| API 클라이언트 | Orval (OpenAPI 코드 생성), Axios |
| 인증 | JWT (Access + Refresh Token) |
| 모니터링 | Spring Actuator, Prometheus (Micrometer) |
| 인프라 | Docker, Docker Compose |

## 시작하기

```bash
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
# 각 .env 파일을 실제 환경에 맞게 수정

docker compose up -d --build
```

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:3000`

### 환경변수

#### Backend (`backend/.env`)

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `SPRING_PROFILES_ACTIVE` | Spring 프로필 | `local` |
| `DB_HOST` | MariaDB 호스트 | `localhost` |
| `DB_PORT` | MariaDB 포트 | `3306` |
| `DB_NAME` | 데이터베이스명 | `stream_archive` |
| `DB_USERNAME` | DB 사용자 | `stream_archive_app` |
| `DB_PASSWORD` | DB 비밀번호 | `password` |
| `SERVER_PORT` | 서버 포트 | `8080` |
| `API_BASE_URL` | API 기본 URL | `http://localhost:8080` |
| `FRONTEND_BASE_URL` | 프론트엔드 URL | `http://localhost:3000` |
| `COOKIE_DOMAIN` | 쿠키 도메인 | `localhost` |
| `ADMIN_USERNAME` | 관리자 계정 | `admin` |
| `ADMIN_PASSWORD` | 관리자 비밀번호 | `admin` |
| `STORAGE_BASE_PATH` | 녹화 파일 저장 경로 | `./storage` |
| `JWT_SECRET` | JWT 서명 키 (256bit 이상) | - |
| `TWITCH_APP_CLIENT_ID` | Twitch API Client ID | - |
| `TWITCH_APP_CLIENT_SECRET` | Twitch API Client Secret | - |
| `TWITCH_PERSONAL_OAUTH_TOKEN` | Twitch OAuth Token | - |

#### Frontend (`frontend/.env`)

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `NEXT_PUBLIC_API_BASE_URL` | API 서버 URL | `http://localhost:8080` |
| `NEXT_PUBLIC_API_IMAGE_PROTOCOL` | 이미지 프로토콜 | `http` |
| `NEXT_PUBLIC_API_IMAGE_HOSTNAME` | 이미지 호스트명 | `localhost` |
| `NEXT_PUBLIC_API_IMAGE_PORT` | 이미지 포트 | `8080` |
| `API_SCHEMA_URL` | OpenAPI 문서 URL | `http://localhost:8080/v3/api-docs` |

### 포트/볼륨 커스터마이징

`docker-compose.override.yml`을 생성하여 기본 설정을 오버라이드할 수 있습니다 (gitignored):

```yaml
services:
  backend:
    ports: !override
      - "8089:8080"

  frontend:
    ports: !override
      - "3009:3000"
```

### macOS + NFS 스토리지 설정

macOS에서 NFS(NAS 등)로 마운트된 경로를 Docker 볼륨으로 사용하려면,
macOS의 NFS 마운트를 바인드 마운트하는 것이 아니라 **Docker에서 직접 NFS 마운트**해야 합니다.

> Docker Desktop for Mac은 Linux VM 위에서 동작하기 때문에,
> macOS에서 마운트한 NFS 경로를 VM이 인식하지 못합니다.

#### 1. NAS NFS 설정

Synology 기준:
- **제어판 → 공유 폴더 → NFS 권한** 에서 해당 폴더의 NFS 규칙 추가
- 허용 클라이언트: Mac의 IP 또는 서브넷 (예: `192.168.1.0/24`)
- **"비특권 포트에서 연결 허용"** 체크 (Docker VM이 비특권 포트를 사용하기 때문에 필수)
- Squash: `모든 사용자를 admin에 매핑`

#### 2. docker-compose.override.yml

```yaml
services:
  backend:
    volumes: !override
      - nfs-storage:/storage

volumes:
  nfs-storage:
    driver: local
    driver_opts:
      type: nfs
      o: addr=<NAS_IP>,nfsvers=3,nolock,soft
      device: ":<NFS_EXPORT_경로>"
```

`<NAS_IP>`와 `<NFS_EXPORT_경로>`는 실제 NAS 설정에 맞게 변경합니다.
NFS export 경로는 macOS에서 `mount | grep nfs`로 확인할 수 있습니다.

```bash
# 예시 출력:
# 192.168.1.11:/volume2/StreamingRecord on /Users/.../nfs/... (nfs, ...)
# → addr=192.168.1.11, device=":/volume2/StreamingRecord/storage"
```

#### 3. NFS 볼륨 재생성

NFS 설정을 변경한 경우 기존 볼륨을 삭제 후 재생성해야 합니다:

```bash
docker compose down
docker volume rm stream-archive_nfs-storage
docker compose up -d
```

## 개발 환경

### 요구사항

- JDK 21
- Node.js 22+
- pnpm
- Docker & Docker Compose
- MariaDB (또는 Docker로 실행)

### Backend

```bash
cd backend
cp .env.example .env  # 환경변수 설정
./gradlew bootRun
```

### Frontend

```bash
cd frontend
cp .env.example .env  # 환경변수 설정
pnpm install
pnpm run dev
```

### API 클라이언트 생성

백엔드의 OpenAPI 스펙으로부터 프론트엔드 API 클라이언트를 자동 생성합니다:

```bash
cd frontend
pnpm run orval          # 1회 생성
pnpm run orval:watch    # 변경 감지 자동 생성
```
