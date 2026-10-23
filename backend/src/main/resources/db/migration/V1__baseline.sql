-- 기준 스키마 (Flyway를 넣은 2026-10-23의 엔티티)
--
-- Flyway를 넣기 전에는 Hibernate(ddl-auto: update)가 엔티티를 보고 테이블을 만들었다.
-- 이 파일은 그때 Hibernate가 만들던 테이블과 같다.
-- 엔티티에 이름을 적지 않은 제약(UK..., FK...)도 Hibernate가 짓던 이름 그대로 둔다.
-- 개발 DB와 운영 DB는 이미 테이블이 있어서, 이 파일을 실행하지 않고 버전 1로 표시만 한다 (spring.flyway.baseline-on-migrate).
-- 그래서 이 파일은 빈 DB에서만 실행된다.

CREATE TABLE users
(
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    uuid          VARCHAR(36)  NOT NULL COMMENT '사용자 UUID',
    username      VARCHAR(100) NOT NULL COMMENT '사용자명',
    name          VARCHAR(100) NOT NULL COMMENT '이름',
    password      VARCHAR(255) NOT NULL COMMENT '비밀번호',
    role          VARCHAR(50)  NOT NULL COMMENT '역할',
    last_login_at DATETIME(6)  NULL COMMENT '마지막 로그인 일시',
    is_active     BIT          NOT NULL COMMENT '활성 상태',
    deleted_at    DATETIME(6)  NULL COMMENT '삭제 일시',
    deleted_by    BIGINT       NULL COMMENT '삭제한 사용자 ID',
    deleted_ip    VARCHAR(45)  NULL COMMENT '삭제 시 IP',
    created_at    DATETIME(6)  NOT NULL COMMENT '생성 일시',
    updated_at    DATETIME(6)  NOT NULL COMMENT '수정 일시',
    created_by    BIGINT       NULL COMMENT '생성한 사용자 ID',
    created_ip    VARCHAR(45)  NULL COMMENT '생성 시 IP',
    updated_by    BIGINT       NULL COMMENT '수정한 사용자 ID',
    updated_ip    VARCHAR(45)  NULL COMMENT '수정 시 IP',

    PRIMARY KEY (id),
    CONSTRAINT UK6km2m9i3vjuy36rnvkgj1l61s UNIQUE (uuid),
    CONSTRAINT UKr43af9ap4edm43mmtq01oddj6 UNIQUE (username),
    INDEX idx_users_username (username),
    INDEX idx_users_role (role),
    INDEX idx_users_is_active (is_active)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_uca1400_ai_ci
  COMMENT = '사용자';

CREATE TABLE channels
(
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    uuid            VARCHAR(36)  NOT NULL COMMENT '채널 UUID',
    name            VARCHAR(255) NOT NULL COMMENT '채널 이름',
    content_privacy VARCHAR(50)  NOT NULL COMMENT '콘텐츠 공개 범위',
    is_active       BIT          NOT NULL COMMENT '활성 상태',
    deleted_at      DATETIME(6)  NULL COMMENT '삭제 일시',
    deleted_by      BIGINT       NULL COMMENT '삭제한 사용자 ID',
    deleted_ip      VARCHAR(45)  NULL COMMENT '삭제 시 IP',
    created_at      DATETIME(6)  NOT NULL COMMENT '생성 일시',
    updated_at      DATETIME(6)  NOT NULL COMMENT '수정 일시',
    created_by      BIGINT       NULL COMMENT '생성한 사용자 ID',
    created_ip      VARCHAR(45)  NULL COMMENT '생성 시 IP',
    updated_by      BIGINT       NULL COMMENT '수정한 사용자 ID',
    updated_ip      VARCHAR(45)  NULL COMMENT '수정 시 IP',

    PRIMARY KEY (id),
    CONSTRAINT UKiix3qhtcokthqmacskecpi038 UNIQUE (uuid),
    INDEX idx_channels_content_privacy (content_privacy),
    INDEX idx_channels_created_at (created_at),
    INDEX idx_channels_is_active (is_active)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_uca1400_ai_ci
  COMMENT = '채널';

CREATE TABLE channel_platforms
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    channel_id          BIGINT       NOT NULL COMMENT '채널',
    platform_type       VARCHAR(50)  NOT NULL COMMENT '플랫폼 유형',
    platform_channel_id VARCHAR(255) NOT NULL COMMENT '플랫폼 채널 ID',
    is_sync_profile     BIT          NOT NULL COMMENT '프로필 동기화 여부',
    is_active           BIT          NOT NULL COMMENT '활성 상태',
    deleted_at          DATETIME(6)  NULL COMMENT '삭제 일시',
    deleted_by          BIGINT       NULL COMMENT '삭제한 사용자 ID',
    deleted_ip          VARCHAR(45)  NULL COMMENT '삭제 시 IP',
    created_at          DATETIME(6)  NOT NULL COMMENT '생성 일시',
    updated_at          DATETIME(6)  NOT NULL COMMENT '수정 일시',
    created_by          BIGINT       NULL COMMENT '생성한 사용자 ID',
    created_ip          VARCHAR(45)  NULL COMMENT '생성 시 IP',
    updated_by          BIGINT       NULL COMMENT '수정한 사용자 ID',
    updated_ip          VARCHAR(45)  NULL COMMENT '수정 시 IP',

    PRIMARY KEY (id),
    INDEX idx_channel_platforms_platform_type (platform_type),
    INDEX idx_channel_platforms_is_active (is_active),
    CONSTRAINT FKj30lbhq2jhrkoatfk7nsqh6mq FOREIGN KEY (channel_id) REFERENCES channels (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_uca1400_ai_ci
  COMMENT = '채널-플랫폼 연동';

CREATE TABLE record_schedules
(
    id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    channel_id     BIGINT      NOT NULL COMMENT '채널',
    platform_type  VARCHAR(50) NOT NULL COMMENT '플랫폼 유형',
    schedule_type  VARCHAR(50) NOT NULL COMMENT '녹화 스케줄 유형',
    value          TEXT        NOT NULL COMMENT '스케줄 값',
    record_quality VARCHAR(50) NOT NULL COMMENT '녹화 화질',
    priority       INTEGER     NOT NULL COMMENT '우선순위',
    auto_archive   BIT         NOT NULL COMMENT '자동 소장 여부',
    is_active      BIT         NOT NULL COMMENT '활성 상태',
    deleted_at     DATETIME(6) NULL COMMENT '삭제 일시',
    deleted_by     BIGINT      NULL COMMENT '삭제한 사용자 ID',
    deleted_ip     VARCHAR(45) NULL COMMENT '삭제 시 IP',
    created_at     DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at     DATETIME(6) NOT NULL COMMENT '수정 일시',
    created_by     BIGINT      NULL COMMENT '생성한 사용자 ID',
    created_ip     VARCHAR(45) NULL COMMENT '생성 시 IP',
    updated_by     BIGINT      NULL COMMENT '수정한 사용자 ID',
    updated_ip     VARCHAR(45) NULL COMMENT '수정 시 IP',

    PRIMARY KEY (id),
    INDEX idx_record_schedules_channel_platform (channel_id, platform_type),
    INDEX idx_record_schedules_type (schedule_type),
    INDEX idx_record_schedules_is_active (is_active),
    INDEX idx_record_schedules_priority (priority),
    CONSTRAINT FK59l7dqn10d3hf2tlqu4jp5lym FOREIGN KEY (channel_id) REFERENCES channels (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_uca1400_ai_ci
  COMMENT = '녹화 스케줄';

CREATE TABLE videos
(
    id                      BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    uuid                    VARCHAR(36)  NOT NULL COMMENT '동영상 UUID',
    channel_id              BIGINT       NOT NULL COMMENT '채널 ID',
    version                 BIGINT       NOT NULL COMMENT '낙관적 락 버전',
    title                   VARCHAR(500) NOT NULL COMMENT '제목',
    description             TEXT         NULL COMMENT '설명',
    duration                INTEGER      NOT NULL COMMENT '재생 시간 (초)',
    file_size               BIGINT       NOT NULL COMMENT '파일 크기 (바이트)',
    content_privacy         VARCHAR(50)  NOT NULL COMMENT '콘텐츠 공개 범위',
    chat_sync_offset_millis BIGINT       NOT NULL COMMENT '채팅 싱크 오프셋 (밀리초)',
    is_archived             BIT          NOT NULL COMMENT '소장 여부',
    archived_at             DATETIME(6)  NULL COMMENT '소장 처리 일시',
    archived_by             BIGINT       NULL COMMENT '소장 처리한 사용자 ID',
    archived_ip             VARCHAR(45)  NULL COMMENT '소장 처리 시 IP',
    is_active               BIT          NOT NULL COMMENT '활성 상태',
    deleted_at              DATETIME(6)  NULL COMMENT '삭제 일시',
    deleted_by              BIGINT       NULL COMMENT '삭제한 사용자 ID',
    deleted_ip              VARCHAR(45)  NULL COMMENT '삭제 시 IP',
    created_at              DATETIME(6)  NOT NULL COMMENT '생성 일시',
    updated_at              DATETIME(6)  NOT NULL COMMENT '수정 일시',
    created_by              BIGINT       NULL COMMENT '생성한 사용자 ID',
    created_ip              VARCHAR(45)  NULL COMMENT '생성 시 IP',
    updated_by              BIGINT       NULL COMMENT '수정한 사용자 ID',
    updated_ip              VARCHAR(45)  NULL COMMENT '수정 시 IP',

    PRIMARY KEY (id),
    CONSTRAINT UK7y9vvf05cq5sbfi3dlebv9mwq UNIQUE (uuid),
    INDEX idx_videos_channel_id (channel_id),
    INDEX idx_videos_content_privacy (content_privacy),
    INDEX idx_videos_created_at (created_at),
    INDEX idx_videos_is_active (is_active),
    INDEX idx_videos_is_archived (is_archived),
    INDEX idx_videos_archived_created (is_archived, created_at),
    CONSTRAINT FKbaube8rnq6coeqb22rt2fv8hh FOREIGN KEY (channel_id) REFERENCES channels (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_uca1400_ai_ci
  COMMENT = '동영상';

CREATE TABLE video_archive_histories
(
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    video_id    BIGINT      NOT NULL COMMENT '동영상 ID',
    is_archived BIT         NOT NULL COMMENT '소장 여부 (해당 시점 전환값)',
    action_by   BIGINT      NULL COMMENT '액션 수행 사용자 ID',
    action_ip   VARCHAR(45) NULL COMMENT '액션 수행 시 IP',
    created_at  DATETIME(6) NOT NULL COMMENT '생성 일시',

    PRIMARY KEY (id),
    INDEX idx_video_archive_histories_video_id (video_id),
    INDEX idx_video_archive_histories_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_uca1400_ai_ci
  COMMENT = '동영상 소장 이력';

CREATE TABLE video_auto_delete_policies
(
    id                BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    channel_id        BIGINT      NULL COMMENT '채널 ID',
    is_enabled        BIT         NOT NULL COMMENT '자동 삭제 활성화 여부',
    delete_after_days INTEGER     NOT NULL COMMENT '생성 후 며칠 지난 동영상을 삭제할지',
    created_at        DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at        DATETIME(6) NOT NULL COMMENT '수정 일시',
    created_by        BIGINT      NULL COMMENT '생성한 사용자 ID',
    created_ip        VARCHAR(45) NULL COMMENT '생성 시 IP',
    updated_by        BIGINT      NULL COMMENT '수정한 사용자 ID',
    updated_ip        VARCHAR(45) NULL COMMENT '수정 시 IP',

    PRIMARY KEY (id),
    CONSTRAINT uk_video_auto_delete_policies_channel_id UNIQUE (channel_id),
    INDEX idx_video_auto_delete_policies_channel_id (channel_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_uca1400_ai_ci
  COMMENT = '동영상 자동 삭제 정책';

CREATE TABLE video_auto_delete_histories
(
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    video_id         BIGINT       NOT NULL COMMENT '동영상 ID',
    channel_id       BIGINT       NOT NULL COMMENT '채널 ID',
    title            VARCHAR(500) NOT NULL COMMENT '삭제 시점 제목',
    file_size        BIGINT       NOT NULL COMMENT '삭제 시점 파일 크기 (바이트)',
    video_created_at DATETIME(6)  NOT NULL COMMENT '동영상 생성 일시',
    created_at       DATETIME(6)  NOT NULL COMMENT '생성 일시',

    PRIMARY KEY (id),
    INDEX idx_video_auto_delete_histories_video_id (video_id),
    INDEX idx_video_auto_delete_histories_channel_id (channel_id),
    INDEX idx_video_auto_delete_histories_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_uca1400_ai_ci
  COMMENT = '동영상 자동 삭제 이력';

CREATE TABLE records
(
    id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    channel_id         BIGINT       NOT NULL COMMENT '채널 ID',
    video_id           BIGINT       NOT NULL COMMENT '동영상 ID',
    platform_type      VARCHAR(50)  NOT NULL COMMENT '플랫폼 유형',
    platform_stream_id VARCHAR(255) NOT NULL COMMENT '플랫폼 스트림 ID',
    record_quality     VARCHAR(50)  NOT NULL COMMENT '녹화 화질',
    is_ended           BIT          NOT NULL COMMENT '종료 여부',
    is_cancelled       BIT          NOT NULL COMMENT '취소 여부',
    is_failed          BIT          NOT NULL COMMENT '녹화 시작 실패 여부',
    ended_at           DATETIME(6)  NULL COMMENT '녹화 종료 일시',
    created_at         DATETIME(6)  NOT NULL COMMENT '생성 일시',

    PRIMARY KEY (id),
    INDEX idx_records_channel_id (channel_id),
    INDEX idx_records_video_id (video_id),
    INDEX idx_records_platform_stream (platform_type, platform_stream_id),
    INDEX idx_records_status (is_ended, is_cancelled),
    CONSTRAINT FKh26dmy5x7urd8asqlxix53n9x FOREIGN KEY (channel_id) REFERENCES channels (id),
    CONSTRAINT FKi2mw76osdkn4hfuwvoh2qti6p FOREIGN KEY (video_id) REFERENCES videos (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_uca1400_ai_ci
  COMMENT = '녹화 기록';

CREATE TABLE video_chat_histories
(
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'ID',
    video_id        BIGINT        NOT NULL COMMENT '동영상 ID',
    message_type    VARCHAR(50)   NOT NULL DEFAULT 'CHAT' COMMENT '메시지 종류 (CHAT/DONATION)',
    username        VARCHAR(255)  NOT NULL COMMENT '사용자명',
    user_role       VARCHAR(50)   NULL COMMENT '작성자 역할 (STREAMER/MANAGER)',
    message         VARCHAR(1000) NOT NULL COMMENT '메시지',
    donation_amount INTEGER       NULL COMMENT '후원 금액',
    emojis          JSON          NULL COMMENT '이모지',
    data            TEXT          NULL COMMENT '원본 데이터',
    offset_millis   BIGINT        NOT NULL COMMENT '동영상 시작 기준 오프셋 (밀리초)',
    created_at      DATETIME(6)   NOT NULL COMMENT '생성 일시',

    PRIMARY KEY (id),
    INDEX idx_video_chat_histories_video_id (video_id),
    INDEX idx_video_chat_histories_created_at (created_at),
    INDEX idx_video_chat_histories_offset (offset_millis)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_uca1400_ai_ci
  COMMENT = '동영상 채팅 이력';

CREATE TABLE video_metadata_viewer_histories
(
    id            BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    video_id      BIGINT      NOT NULL COMMENT '동영상 ID',
    viewer_count  INTEGER     NOT NULL COMMENT '시청자 수',
    offset_millis BIGINT      NOT NULL COMMENT '동영상 시작 기준 오프셋 (밀리초)',
    created_at    DATETIME(6) NOT NULL COMMENT '생성 일시',

    PRIMARY KEY (id),
    INDEX idx_video_metadata_viewer_histories_video_id (video_id),
    INDEX idx_video_metadata_viewer_histories_created_at (created_at),
    INDEX idx_video_metadata_viewer_histories_offset (offset_millis)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_uca1400_ai_ci
  COMMENT = '동영상 시청자 수 이력';

CREATE TABLE video_metadata_title_histories
(
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    video_id      BIGINT       NOT NULL COMMENT '동영상 ID',
    title         VARCHAR(500) NOT NULL COMMENT '제목',
    offset_millis BIGINT       NOT NULL COMMENT '동영상 시작 기준 오프셋 (밀리초)',
    created_at    DATETIME(6)  NOT NULL COMMENT '생성 일시',

    PRIMARY KEY (id),
    INDEX idx_video_metadata_title_histories_video_id (video_id),
    INDEX idx_video_metadata_title_histories_created_at (created_at),
    INDEX idx_video_metadata_title_histories_offset (offset_millis)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_uca1400_ai_ci
  COMMENT = '동영상 제목 변경 이력';

CREATE TABLE video_metadata_category_histories
(
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    video_id      BIGINT       NOT NULL COMMENT '동영상 ID',
    category      VARCHAR(255) NULL COMMENT '카테고리',
    offset_millis BIGINT       NOT NULL COMMENT '동영상 시작 기준 오프셋 (밀리초)',
    created_at    DATETIME(6)  NOT NULL COMMENT '생성 일시',

    PRIMARY KEY (id),
    INDEX idx_video_metadata_category_histories_video_id (video_id),
    INDEX idx_video_metadata_category_histories_created_at (created_at),
    INDEX idx_video_metadata_category_histories_offset (offset_millis)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_uca1400_ai_ci
  COMMENT = '동영상 카테고리 변경 이력';

CREATE TABLE user_video_watch_histories
(
    id            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '시청 기록 ID',
    user_id       BIGINT      NOT NULL COMMENT '사용자 ID',
    video_id      BIGINT      NOT NULL COMMENT '동영상 ID',
    last_position INTEGER     NOT NULL COMMENT '마지막 재생 위치 (초)',
    watched_at    DATETIME(6) NOT NULL COMMENT '시청 일시',

    PRIMARY KEY (id),
    CONSTRAINT uk_user_video UNIQUE (user_id, video_id),
    INDEX idx_user_watched (user_id, watched_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_uca1400_ai_ci
  COMMENT = '동영상 시청 기록';

CREATE TABLE refresh_tokens
(
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    user_id    BIGINT       NOT NULL COMMENT '사용자 ID',
    token      VARCHAR(500) NOT NULL COMMENT '토큰',
    expires_at DATETIME(6)  NOT NULL COMMENT '만료 일시',
    is_active  BIT          NOT NULL COMMENT '활성 상태',
    deleted_at DATETIME(6)  NULL COMMENT '삭제 일시',
    deleted_by BIGINT       NULL COMMENT '삭제한 사용자 ID',
    deleted_ip VARCHAR(45)  NULL COMMENT '삭제 시 IP',
    created_at DATETIME(6)  NOT NULL COMMENT '생성 일시',
    updated_at DATETIME(6)  NOT NULL COMMENT '수정 일시',
    created_by BIGINT       NULL COMMENT '생성한 사용자 ID',
    created_ip VARCHAR(45)  NULL COMMENT '생성 시 IP',
    updated_by BIGINT       NULL COMMENT '수정한 사용자 ID',
    updated_ip VARCHAR(45)  NULL COMMENT '수정 시 IP',

    PRIMARY KEY (id),
    CONSTRAINT UKghpmfn23vmxfu3spu3lfg4r2d UNIQUE (token),
    INDEX idx_refresh_tokens_user_id (user_id),
    INDEX idx_refresh_tokens_token (token),
    INDEX idx_refresh_tokens_expires_at (expires_at),
    INDEX idx_refresh_tokens_is_active (is_active)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_uca1400_ai_ci
  COMMENT = '리프레시 토큰';

CREATE TABLE user_settings
(
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    user_id       BIGINT       NOT NULL COMMENT '사용자 ID',
    setting_key   VARCHAR(50)  NOT NULL COMMENT '설정 키 (SHOW_UNLISTED_CHANNELS/SHOW_UNLISTED_VIDEOS)',
    setting_value VARCHAR(255) NOT NULL COMMENT '설정 값',
    created_at    DATETIME(6)  NOT NULL COMMENT '생성 일시',
    updated_at    DATETIME(6)  NOT NULL COMMENT '수정 일시',
    created_by    BIGINT       NULL COMMENT '생성한 사용자 ID',
    created_ip    VARCHAR(45)  NULL COMMENT '생성 시 IP',
    updated_by    BIGINT       NULL COMMENT '수정한 사용자 ID',
    updated_ip    VARCHAR(45)  NULL COMMENT '수정 시 IP',

    PRIMARY KEY (id),
    CONSTRAINT uk_user_settings_user_id_setting_key UNIQUE (user_id, setting_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_uca1400_ai_ci
  COMMENT = '사용자 설정';
