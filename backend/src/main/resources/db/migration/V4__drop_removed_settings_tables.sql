-- 지운 설정 기능의 테이블을 지운다 (2026-10-23)
--
-- 전역 설정과 채널별 설정 기능은 2026-06-12에 엔티티째 지웠다 (a284188).
-- ddl-auto: update가 테이블은 지우지 않아서 운영 DB와 개발 DB에 남아 있다.
-- global_settings에는 그 기능이 기동할 때 넣던 기본값 행만 있고, 이제 읽는 코드가 없다.
-- V1로 만든 DB에는 두 테이블이 없다.

DROP TABLE IF EXISTS channel_settings;

DROP TABLE IF EXISTS global_settings;
