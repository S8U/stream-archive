-- Stream Archive Database Schema

-- Enum 타입들
-- PlatformType: 'CHZZK', 'TWITCH', 'SOOP', 'YOUTUBE'
-- ContentPrivacy: 'PUBLIC', 'UNLISTED', 'PRIVATE'
-- RecordScheduleType: 'ONCE', 'ALWAYS', 'N_DAYS_OF_EVERY_WEEK', 'SPECIFIC_DAY'
-- Role: 'ADMIN', 'USER'

-- 1. 사용자 테이블
CREATE TABLE users
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid       VARCHAR(36)  NOT NULL UNIQUE,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    role       ENUM ('ADMIN', 'USER') NOT NULL DEFAULT 'USER',
    created_at DATETIME     NOT NULL        DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_users_username (username),
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
);

-- 2. 채널 테이블
CREATE TABLE channels
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid            VARCHAR(36)                            NOT NULL UNIQUE,
    name            VARCHAR(255)                           NOT NULL,
    content_privacy ENUM ('PUBLIC', 'UNLISTED', 'PRIVATE') NOT NULL,
    created_at      DATETIME                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_channels_content_privacy (content_privacy),
    INDEX idx_channels_created_at (created_at)
);

-- 3. 채널-플랫폼 연동 테이블
CREATE TABLE channel_platforms
(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    channel_id          BIGINT                                      NOT NULL,
    platform_type       ENUM ('CHZZK', 'TWITCH', 'SOOP', 'YOUTUBE') NOT NULL,
    platform_channel_id VARCHAR(255)                                NOT NULL,
    is_sync_profile     BOOLEAN                                     NOT NULL DEFAULT TRUE,
    created_at          DATETIME                                    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME                                    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    UNIQUE KEY uk_channel_platforms (channel_id, platform_type),
    INDEX idx_channel_platforms_platform_type (platform_type)
);

-- 4. 녹화 스케줄 테이블
CREATE TABLE record_schedules
(
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    channel_id           BIGINT                                                            NOT NULL,
    platform_type        ENUM ('CHZZK', 'TWITCH', 'SOOP', 'YOUTUBE')                       NOT NULL,
    record_schedule_type ENUM ('ONCE', 'ALWAYS', 'N_DAYS_OF_EVERY_WEEK', 'SPECIFIC_DAY') NOT NULL,
    value                TEXT                                                              NOT NULL,
    created_at           DATETIME                                                          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME                                                          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    INDEX idx_record_schedules_channel_platform (channel_id, platform_type),
    INDEX idx_record_schedules_type (record_schedule_type)
);

-- 5. 비디오 테이블 (녹화 세션)
CREATE TABLE videos
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid            VARCHAR(36)                            NOT NULL UNIQUE,
    channel_id      BIGINT                                 NOT NULL,
    title           VARCHAR(500)                           NOT NULL,
    duration        INT                                    NOT NULL DEFAULT 0,
    file_size       BIGINT                                 NOT NULL DEFAULT 0,
    content_privacy ENUM ('PUBLIC', 'UNLISTED', 'PRIVATE') NOT NULL,
    created_at      DATETIME                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    INDEX idx_videos_channel_id (channel_id),
    INDEX idx_videos_content_privacy (content_privacy),
    INDEX idx_videos_created_at (created_at)
);

-- 6. 녹화 기록 테이블
CREATE TABLE records
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    channel_id         BIGINT                                      NOT NULL,
    video_id           BIGINT                                      NOT NULL,
    platform_type      ENUM ('CHZZK', 'TWITCH', 'SOOP', 'YOUTUBE') NOT NULL,
    platform_stream_id VARCHAR(255)                                NOT NULL,
    record_quality     VARCHAR(50)                                 NOT NULL,
    is_ended           BOOLEAN                                     NOT NULL DEFAULT FALSE,
    is_cancelled       BOOLEAN                                     NOT NULL DEFAULT FALSE,
    created_at         DATETIME                                    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at           DATETIME                                    NULL,

    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    INDEX idx_records_channel_id (channel_id),
    INDEX idx_records_video_id (video_id),
    INDEX idx_records_platform_stream (platform_type, platform_stream_id),
    INDEX idx_records_status (is_ended, is_cancelled)
);

-- 7. 채팅 이력 테이블
CREATE TABLE video_chat_histories
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    video_id      BIGINT        NOT NULL,
    username      VARCHAR(255)  NOT NULL,
    message       VARCHAR(1000) NOT NULL,
    data          TEXT          NULL,
    offset_millis BIGINT        NOT NULL,
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    INDEX idx_video_chat_histories_video_id (video_id),
    INDEX idx_video_chat_histories_created_at (created_at),
    INDEX idx_video_chat_histories_offset (offset_millis)
);

-- 8. 시청자 수 이력 테이블
CREATE TABLE video_viewer_histories
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    video_id      BIGINT   NOT NULL,
    viewer_count  INT      NOT NULL,
    offset_millis BIGINT   NOT NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    INDEX idx_video_viewer_histories_video_id (video_id),
    INDEX idx_video_viewer_histories_created_at (created_at),
    INDEX idx_video_viewer_histories_offset (offset_millis)
);

-- 9. 제목 변경 이력 테이블
CREATE TABLE video_title_histories
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    video_id      BIGINT       NOT NULL,
    title         VARCHAR(500) NOT NULL,
    offset_millis BIGINT       NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    INDEX idx_video_title_histories_video_id (video_id),
    INDEX idx_video_title_histories_created_at (created_at),
    INDEX idx_video_title_histories_offset (offset_millis)
);

-- 10. 카테고리 변경 이력 테이블
CREATE TABLE video_category_histories
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    video_id      BIGINT       NOT NULL,
    category      VARCHAR(255) NULL,
    offset_millis BIGINT       NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    INDEX idx_video_category_histories_video_id (video_id),
    INDEX idx_video_category_histories_created_at (created_at),
    INDEX idx_video_category_histories_offset (offset_millis)
);

-- 11. 공통 설정 테이블
CREATE TABLE global_settings
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key   VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT         NOT NULL,
    description   VARCHAR(500) NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_global_settings_key (setting_key)
);

-- 12. 채널별 설정 테이블
CREATE TABLE channel_settings
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    channel_id    BIGINT       NOT NULL,
    setting_key   VARCHAR(100) NOT NULL,
    setting_value TEXT         NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    UNIQUE KEY uk_channel_setting (channel_id, setting_key),
    INDEX idx_channel_settings_channel_id (channel_id)
);

-- 13. 시청 기록 테이블
CREATE TABLE video_watch_histories
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT   NOT NULL,
    video_id      BIGINT   NOT NULL,
    last_position INT      NOT NULL,
    watched_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_video (user_id, video_id),
    INDEX idx_user_watched (user_id, watched_at)
);