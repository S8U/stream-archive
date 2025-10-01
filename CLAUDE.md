# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Stream Archive is a multi-platform streaming recording system that automatically detects and records broadcasts from various streaming platforms (Chzzk, Twitch, SOOP, YouTube) and builds a personal archive web application.

## Architecture

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
npm run dev          # Start development server with Turbopack
npm run build        # Build for production with Turbopack
npm run start        # Start production server
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