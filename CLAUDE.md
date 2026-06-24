# CLAUDE.md

This file provides guidance when working with code in this repository.

## Project Overview

Stream Archive is a multi-platform streaming recording system that automatically detects and records broadcasts from various streaming platforms (Chzzk, Twitch, SOOP, YouTube) and builds a personal archive web application.

## Architecture

> **IMPORTANT**: 백엔드 코드를 작성·수정하기 전에 **반드시 `docs/architecture.md`를 먼저 읽고** 그 아키텍처 원칙(레이어 의존 방향, UseCase 분리, 엔티티 규칙 위치, 네이밍 어순 등)을 따른다. 작업 후 architecture-reviewer 훅이 변경분을 검토한다.

This is a full-stack application with the following structure:

### Backend
- **Technology**: Spring Boot 3.5.6 with Kotlin
- **Database**: MariaDB with JPA/Hibernate
- **Location**: `backend/` directory
- **Java Version**: Requires JDK 21
- **Key Dependencies**: Spring Security, Spring Data JPA, Actuator with Prometheus metrics

### Frontend
- **Technology**: Next.js 15.5.4 with React 19 and TypeScript
- **Styling**: Tailwind CSS v4
- **Location**: `frontend/` directory

### Database Schema
The complete database schema is documented in `docs/schema.sql` with the following key entities:
- **Channels**: User-created channels that can be linked to multiple streaming platforms
- **Channel Platforms**: Links between channels and streaming platform IDs
- **Record Schedules**: Configurable recording schedules (once, always, weekly, specific dates)
- **Videos**: Recorded content with HLS format for real-time viewing during recording
- **Records**: Active recording sessions with platform stream IDs for duplicate prevention
- **Chat/Viewer/Title/Category Histories**: Time-indexed metadata collected during recording

## Development Commands

### Frontend
```bash
cd frontend
pnpm run dev          # Start development server with Turbopack
pnpm run build        # Build for production with Turbopack
pnpm run start        # Start production server
```

### Backend
```bash
cd backend
./gradlew build      # Build the application (requires JDK 21)
./gradlew test       # Run tests
./gradlew bootRun    # Run the application
```

**Note**: Backend requires JDK 21. The Gradle build will fail with Java 8.

### Database
```bash
docker-compose -f docker-compose-local.yml up -d  # Start local MariaDB
```
- Database: `stream_archive`
- User: `stream_archive_app`
- Password: `password`
- Port: `3306`

## Key Features

### Recording System
- Uses streamlink → pipe → ffmpeg for HLS recording
- Real-time viewing capability during recording
- Duplicate recording prevention via platform stream ID tracking
- Multi-data collection: video, chat, viewer count, title changes (30s intervals)

### Channel Management
- Custom channel names with multi-platform linking
- Privacy levels: PUBLIC, UNLISTED, PRIVATE
- Automatic profile sync from platform APIs

### Scheduling
- Flexible recording schedules per channel+platform combination
- Types: once, always, weekly, specific dates
- Background polling system for broadcast detection

## Important Notes

- The system is designed for personal archiving use
- Recording uses HLS format to enable real-time viewing during recording
- Chat and metadata are collected via WebSocket connections
- All video metadata (viewer count, title changes) are tracked with millisecond-precision timestamps for playback synchronization

## 커밋 컨벤션

- Conventional Commit + 한글. (`feat`, `fix`, `chore`, `refactor`, `docs`, `test` 등)
- **scope는 최상위 모듈명만 쓴다: `backend` / `frontend`.** 도메인(order, payment 등)은 scope에 넣지 않고 제목 본문이 말해준다.
  - `feat(backend): 주문 생성 API 추가`
  - `fix(backend): 결제 금액 검증 버그 수정`
  - `feat(frontend): 장바구니 페이지 추가`
- 여러 모듈에 걸치거나 모듈 밖(루트 설정 등) 변경이면 scope를 생략한다.
- `Co-Authored-By`는 쓰지 않는다.