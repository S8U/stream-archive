-- enum 컬럼을 DB ENUM 타입에서 VARCHAR(50)으로 바꾼다 (2026-10-23)
--
-- 엔티티의 enum 필드를 @Enumerated 대신 EnumNameConverter로 저장하게 바꿨다.
-- 새로 만드는 테이블은 처음부터 VARCHAR(50)이지만, ddl-auto는 이미 있는 컬럼을 고치지 않아서 직접 바꾼다.
-- 바꾸지 않으면 enum에 값을 더할 때마다 이 컬럼들의 ENUM 값 목록을 직접 고쳐야 한다.
--
-- 돌리는 곳: 개발 DB(stream_archive_dev)와 운영 DB(stream_archive)에 각각 한 번.
-- 돌리는 때: 코드 배포 전이든 후든 된다. 새 코드는 ENUM 컬럼에도 읽고 쓰고, 이전 코드도 VARCHAR 컬럼에 읽고 쓴다.
-- 주의: video_chat_histories는 행이 많아 테이블을 복사하는 동안 쓰기가 막힌다. 녹화 중이 아닐 때 돌린다.
--
-- NULL 여부, 기본값, 설명은 엔티티(@Column, @ColumnDefault, @Comment)와 같게 다시 적는다.
-- MODIFY는 빠뜨린 속성을 지우기 때문이다. 인덱스는 그대로 남는다.

ALTER TABLE users
    MODIFY COLUMN role VARCHAR(50) NOT NULL COMMENT '역할';

ALTER TABLE channels
    MODIFY COLUMN content_privacy VARCHAR(50) NOT NULL COMMENT '콘텐츠 공개 범위';

ALTER TABLE channel_platforms
    MODIFY COLUMN platform_type VARCHAR(50) NOT NULL COMMENT '플랫폼 유형';

ALTER TABLE record_schedules
    MODIFY COLUMN platform_type VARCHAR(50) NOT NULL COMMENT '플랫폼 유형',
    MODIFY COLUMN schedule_type VARCHAR(50) NOT NULL COMMENT '녹화 스케줄 유형',
    MODIFY COLUMN record_quality VARCHAR(50) NOT NULL COMMENT '녹화 화질';

ALTER TABLE records
    MODIFY COLUMN platform_type VARCHAR(50) NOT NULL COMMENT '플랫폼 유형';

ALTER TABLE videos
    MODIFY COLUMN content_privacy VARCHAR(50) NOT NULL COMMENT '콘텐츠 공개 범위';

ALTER TABLE video_chat_histories
    MODIFY COLUMN message_type VARCHAR(50) NOT NULL DEFAULT 'CHAT' COMMENT '메시지 종류 (CHAT/DONATION)',
    MODIFY COLUMN user_role VARCHAR(50) NULL COMMENT '작성자 역할 (STREAMER/MANAGER)';
