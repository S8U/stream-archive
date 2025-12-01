-- Stream Archive Database Schema

-- Enum 타입들
-- PlatformType: 'CHZZK', 'TWITCH', 'SOOP'
-- ContentPrivacy: 'PUBLIC', 'UNLISTED', 'PRIVATE'
-- RecordScheduleType: 'ONCE', 'ALWAYS', 'N_DAYS_OF_EVERY_WEEK', 'SPECIFIC_DAY'
-- Role: 'ADMIN', 'USER'

-- 1. 사용자 테이블
CREATE TABLE users
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '사용자 ID',
    uuid            VARCHAR(36)  NOT NULL UNIQUE COMMENT '사용자 UUID',
    username        VARCHAR(100) NOT NULL UNIQUE COMMENT '사용자명',
    password        VARCHAR(255) NOT NULL COMMENT '비밀번호',
    email           VARCHAR(255) NOT NULL UNIQUE COMMENT '이메일',
    role            ENUM ('ADMIN', 'USER') NOT NULL DEFAULT 'USER' COMMENT '역할',
    last_login_at   DATETIME     NULL COMMENT '마지막 로그인 일시',
    is_active       BOOLEAN      NOT NULL        DEFAULT TRUE COMMENT '활성 상태',
    deleted_at      DATETIME     NULL COMMENT '삭제 일시',
    deleted_by      BIGINT       NULL COMMENT '삭제한 사용자 ID',
    deleted_ip      VARCHAR(45)  NULL COMMENT '삭제 시 IP',
    created_at      DATETIME     NOT NULL        DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    created_by      BIGINT       NULL COMMENT '생성한 사용자 ID',
    created_ip      VARCHAR(45)  NULL COMMENT '생성 시 IP',
    updated_at      DATETIME     NOT NULL        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    updated_by      BIGINT       NULL COMMENT '수정한 사용자 ID',
    updated_ip      VARCHAR(45)  NULL COMMENT '수정 시 IP',

    INDEX idx_users_username (username),
    INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_is_active (is_active)
) COMMENT '사용자';

-- 2. 채널 테이블
CREATE TABLE channels
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '채널 ID',
    uuid            VARCHAR(36)                            NOT NULL UNIQUE COMMENT '채널 UUID',
    name            VARCHAR(255)                           NOT NULL COMMENT '채널 이름',
    content_privacy ENUM ('PUBLIC', 'UNLISTED', 'PRIVATE') NOT NULL COMMENT '콘텐츠 공개 범위',
    is_active       BOOLEAN                                NOT NULL DEFAULT TRUE COMMENT '활성 상태',
    deleted_at      DATETIME                               NULL COMMENT '삭제 일시',
    deleted_by      BIGINT                                 NULL COMMENT '삭제한 사용자 ID',
    deleted_ip      VARCHAR(45)                            NULL COMMENT '삭제 시 IP',
    created_at      DATETIME                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    created_by      BIGINT                                 NULL COMMENT '생성한 사용자 ID',
    created_ip      VARCHAR(45)                            NULL COMMENT '생성 시 IP',
    updated_at      DATETIME                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    updated_by      BIGINT                                 NULL COMMENT '수정한 사용자 ID',
    updated_ip      VARCHAR(45)                            NULL COMMENT '수정 시 IP',

    INDEX idx_channels_content_privacy (content_privacy),
    INDEX idx_channels_created_at (created_at),
    INDEX idx_channels_is_active (is_active)
) COMMENT '채널';

-- 3. 채널-플랫폼 연동 테이블
CREATE TABLE channel_platforms
(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '채널-플랫폼 연동 ID',
    channel_id          BIGINT                                      NOT NULL COMMENT '채널 ID',
    platform_type       ENUM ('CHZZK', 'TWITCH', 'SOOP') NOT NULL COMMENT '플랫폼 유형',
    platform_channel_id VARCHAR(255)                                NOT NULL COMMENT '플랫폼 채널 ID',
    is_sync_profile     BOOLEAN                                     NOT NULL DEFAULT TRUE COMMENT '프로필 동기화 여부',
    is_active           BOOLEAN                                     NOT NULL DEFAULT TRUE COMMENT '활성 상태',
    deleted_at          DATETIME                                    NULL COMMENT '삭제 일시',
    deleted_by          BIGINT                                      NULL COMMENT '삭제한 사용자 ID',
    deleted_ip          VARCHAR(45)                                 NULL COMMENT '삭제 시 IP',
    created_at          DATETIME                                    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    created_by          BIGINT                                      NULL COMMENT '생성한 사용자 ID',
    created_ip          VARCHAR(45)                                 NULL COMMENT '생성 시 IP',
    updated_at          DATETIME                                    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    updated_by          BIGINT                                      NULL COMMENT '수정한 사용자 ID',
    updated_ip          VARCHAR(45)                                 NULL COMMENT '수정 시 IP',

    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    UNIQUE KEY uk_channel_platforms (channel_id, platform_type),
    INDEX idx_channel_platforms_platform_type (platform_type),
    INDEX idx_channel_platforms_is_active (is_active)
) COMMENT '채널-플랫폼 연동';

-- 4. 녹화 스케줄 테이블
CREATE TABLE record_schedules
(
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '녹화 스케줄 ID',
    channel_id           BIGINT                                                            NOT NULL COMMENT '채널 ID',
    platform_type        ENUM ('CHZZK', 'TWITCH', 'SOOP')                       NOT NULL COMMENT '플랫폼 유형',
    schedule_type        ENUM ('ONCE', 'ALWAYS', 'N_DAYS_OF_EVERY_WEEK', 'SPECIFIC_DAY')   NOT NULL COMMENT '녹화 스케줄 유형',
    value                TEXT                                                              NOT NULL COMMENT '스케줄 값',
    record_quality       ENUM ('BEST', 'P2160_60', 'P2160', 'P1440_60', 'P1440', 'P1080_60', 'P1080', 'P720_60', 'P720', 'P480', 'P240', 'P144', 'WORST') NOT NULL DEFAULT 'BEST' COMMENT '녹화 화질',
    priority             INT                                                               NOT NULL DEFAULT 0 COMMENT '우선순위',
    is_active            BOOLEAN                                                           NOT NULL DEFAULT TRUE COMMENT '활성 상태',
    deleted_at           DATETIME                                                          NULL COMMENT '삭제 일시',
    deleted_by           BIGINT                                                            NULL COMMENT '삭제한 사용자 ID',
    deleted_ip           VARCHAR(45)                                                       NULL COMMENT '삭제 시 IP',
    created_at           DATETIME                                                          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    created_by           BIGINT                                                            NULL COMMENT '생성한 사용자 ID',
    created_ip           VARCHAR(45)                                                       NULL COMMENT '생성 시 IP',
    updated_at           DATETIME                                                          NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    updated_by           BIGINT                                                            NULL COMMENT '수정한 사용자 ID',
    updated_ip           VARCHAR(45)                                                       NULL COMMENT '수정 시 IP',

    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    INDEX idx_record_schedules_channel_platform (channel_id, platform_type),
    INDEX idx_record_schedules_type (schedule_type),
    INDEX idx_record_schedules_is_active (is_active),
    INDEX idx_record_schedules_priority (priority)
) COMMENT '녹화 스케줄';

-- 5. 동영상 테이블 (녹화 세션)
CREATE TABLE videos
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '동영상 ID',
    uuid            VARCHAR(36)                            NOT NULL UNIQUE COMMENT '동영상 UUID',
    channel_id      BIGINT                                 NOT NULL COMMENT '채널 ID',
    title           VARCHAR(500)                           NOT NULL COMMENT '제목',
    duration        INT                                    NOT NULL DEFAULT 0 COMMENT '재생 시간 (초)',
    file_size       BIGINT                                 NOT NULL DEFAULT 0 COMMENT '파일 크기 (바이트)',
    content_privacy ENUM ('PUBLIC', 'UNLISTED', 'PRIVATE') NOT NULL COMMENT '콘텐츠 공개 범위',
    is_active       BOOLEAN                                NOT NULL DEFAULT TRUE COMMENT '활성 상태',
    deleted_at      DATETIME                               NULL COMMENT '삭제 일시',
    deleted_by      BIGINT                                 NULL COMMENT '삭제한 사용자 ID',
    deleted_ip      VARCHAR(45)                            NULL COMMENT '삭제 시 IP',
    created_at      DATETIME                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    created_by      BIGINT                                 NULL COMMENT '생성한 사용자 ID',
    created_ip      VARCHAR(45)                            NULL COMMENT '생성 시 IP',
    updated_at      DATETIME                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    updated_by      BIGINT                                 NULL COMMENT '수정한 사용자 ID',
    updated_ip      VARCHAR(45)                            NULL COMMENT '수정 시 IP',

    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    INDEX idx_videos_channel_id (channel_id),
    INDEX idx_videos_content_privacy (content_privacy),
    INDEX idx_videos_created_at (created_at),
    INDEX idx_videos_is_active (is_active)
) COMMENT '동영상';

-- 6. 녹화 기록 테이블
CREATE TABLE records
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '녹화 기록 ID',
    channel_id         BIGINT                                      NOT NULL COMMENT '채널 ID',
    video_id           BIGINT                                      NOT NULL COMMENT '동영상 ID',
    platform_type      ENUM ('CHZZK', 'TWITCH', 'SOOP') NOT NULL COMMENT '플랫폼 유형',
    platform_stream_id VARCHAR(255)                                NOT NULL COMMENT '플랫폼 스트림 ID',
    record_quality     VARCHAR(50)                                 NOT NULL COMMENT '녹화 화질',
    is_ended           BOOLEAN                                     NOT NULL DEFAULT FALSE COMMENT '종료 여부',
    is_cancelled       BOOLEAN                                     NOT NULL DEFAULT FALSE COMMENT '취소 여부',
    created_at         DATETIME                                    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '녹화 시작 일시',
    ended_at           DATETIME                                    NULL COMMENT '녹화 종료 일시',

    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    INDEX idx_records_channel_id (channel_id),
    INDEX idx_records_video_id (video_id),
    INDEX idx_records_platform_stream (platform_type, platform_stream_id),
    INDEX idx_records_status (is_ended, is_cancelled)
) COMMENT '녹화 기록';

-- 7. 채팅 이력 테이블
CREATE TABLE video_metadata_chat_histories
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '채팅 이력 ID',
    video_id      BIGINT        NOT NULL COMMENT '동영상 ID',
    username      VARCHAR(255)  NOT NULL COMMENT '사용자명',
    message       VARCHAR(1000) NOT NULL COMMENT '메시지',
    data          TEXT          NULL COMMENT '원본 데이터',
    offset_millis BIGINT        NOT NULL COMMENT '동영상 시작 기준 오프셋 (밀리초)',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',

    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    INDEX idx_video_metadata_chat_histories_video_id (video_id),
    INDEX idx_video_metadata_chat_histories_created_at (created_at),
    INDEX idx_video_metadata_chat_histories_offset (offset_millis)
) COMMENT '동영상 채팅 이력';

-- 8. 시청자 수 이력 테이블
CREATE TABLE video_metadata_viewer_histories
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '시청자 수 이력 ID',
    video_id      BIGINT   NOT NULL COMMENT '동영상 ID',
    viewer_count  INT      NOT NULL COMMENT '시청자 수',
    offset_millis BIGINT   NOT NULL COMMENT '동영상 시작 기준 오프셋 (밀리초)',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',

    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    INDEX idx_video_metadata_viewer_histories_video_id (video_id),
    INDEX idx_video_metadata_viewer_histories_created_at (created_at),
    INDEX idx_video_metadata_viewer_histories_offset (offset_millis)
) COMMENT '동영상 시청자 수 이력';

-- 9. 제목 변경 이력 테이블
CREATE TABLE video_metadata_title_histories
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '제목 변경 이력 ID',
    video_id      BIGINT       NOT NULL COMMENT '동영상 ID',
    title         VARCHAR(500) NOT NULL COMMENT '제목',
    offset_millis BIGINT       NOT NULL COMMENT '동영상 시작 기준 오프셋 (밀리초)',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',

    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    INDEX idx_video_metadata_title_histories_video_id (video_id),
    INDEX idx_video_metadata_title_histories_created_at (created_at),
    INDEX idx_video_metadata_title_histories_offset (offset_millis)
) COMMENT '동영상 제목 변경 이력';

-- 10. 카테고리 변경 이력 테이블
CREATE TABLE video_metadata_category_histories
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '카테고리 변경 이력 ID',
    video_id      BIGINT       NOT NULL COMMENT '동영상 ID',
    category      VARCHAR(255) NULL COMMENT '카테고리',
    offset_millis BIGINT       NOT NULL COMMENT '동영상 시작 기준 오프셋 (밀리초)',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',

    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    INDEX idx_video_metadata_category_histories_video_id (video_id),
    INDEX idx_video_metadata_category_histories_created_at (created_at),
    INDEX idx_video_metadata_category_histories_offset (offset_millis)
) COMMENT '동영상 카테고리 변경 이력';

-- 11. 시청 기록 테이블
CREATE TABLE user_video_watch_histories
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '시청 기록 ID',
    user_id       BIGINT   NOT NULL COMMENT '사용자 ID',
    video_id      BIGINT   NOT NULL COMMENT '동영상 ID',
    last_position INT      NOT NULL COMMENT '마지막 재생 위치 (초)',
    watched_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '시청 일시',

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_video (user_id, video_id),
    INDEX idx_user_watched (user_id, watched_at)
) COMMENT '동영상 시청 기록';

-- 12. 공통 설정 테이블
CREATE TABLE global_settings
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '전역 설정 ID',
    setting_key   VARCHAR(100) NOT NULL UNIQUE COMMENT '설정 키',
    setting_value TEXT         NOT NULL COMMENT '설정 값',
    description   VARCHAR(500) NULL COMMENT '설명',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    INDEX idx_global_settings_key (setting_key)
) COMMENT '전역 설정';

-- 13. 채널별 설정 테이블
CREATE TABLE channel_settings
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '채널별 설정 ID',
    channel_id    BIGINT       NOT NULL COMMENT '채널 ID',
    setting_key   VARCHAR(100) NOT NULL COMMENT '설정 키',
    setting_value TEXT         NOT NULL COMMENT '설정 값',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    UNIQUE KEY uk_channel_setting (channel_id, setting_key),
    INDEX idx_channel_settings_channel_id (channel_id)
) COMMENT '채널별 설정';

-- 14. 리프레시 토큰 테이블
CREATE TABLE refresh_tokens
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '리프레시 토큰 ID',
    user_id    BIGINT       NOT NULL COMMENT '사용자 ID',
    token      VARCHAR(500) NOT NULL UNIQUE COMMENT '토큰',
    expires_at DATETIME     NOT NULL COMMENT '만료 일시',
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '활성 상태',
    deleted_at DATETIME     NULL COMMENT '삭제 일시',
    deleted_by BIGINT       NULL COMMENT '삭제한 사용자 ID',
    deleted_ip VARCHAR(45)  NULL COMMENT '삭제 시 IP',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    created_by BIGINT       NULL COMMENT '생성한 사용자 ID',
    created_ip VARCHAR(45)  NULL COMMENT '생성 시 IP',

    INDEX idx_refresh_tokens_user_id (user_id),
    INDEX idx_refresh_tokens_token (token),
    INDEX idx_refresh_tokens_expires_at (expires_at),
    INDEX idx_refresh_tokens_is_active (is_active)
) COMMENT '리프레시 토큰';