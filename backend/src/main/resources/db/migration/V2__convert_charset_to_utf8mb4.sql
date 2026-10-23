-- DB 기본 문자셋과 armscii8 테이블을 utf8mb4로 바꾼다 (2026-10-23)
--
-- 개발 DB(stream_archive_dev)는 기본 문자셋이 armscii8로 만들어져 있었다.
-- 그래서 그 뒤에 Hibernate가 만든 user_settings, video_archive_histories가 armscii8이고, 한글을 담지 못한다.
-- 두 테이블에는 영문 값(설정 키, IP)만 있어서 바꿔도 깨지는 값이 없다.
-- 이미 utf8mb4인 DB에서는 바뀌는 것이 없다.

ALTER DATABASE CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_ai_ci;

ALTER TABLE user_settings CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_ai_ci;

ALTER TABLE video_archive_histories CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_ai_ci;
