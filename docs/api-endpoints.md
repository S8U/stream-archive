# API 엔드포인트 목록

## 범례
- ✅ 구현 완료
- ❌ 미구현
- 🔒 인증 필요
- 👑 관리자 권한 필요

---

## 1. 인증 API

### 1.1 인증/인가
| 메서드 | 엔드포인트 | 설명 | 상태 |
|--------|-----------|------|------|
| POST | `/auth/login` | 로그인 (JWT 발급) | ✅ |
| POST | `/auth/signup` | 회원가입 | ✅ |
| POST | `/auth/logout` | 로그아웃 | ✅ |
| POST | `/auth/refresh` | 토큰 갱신 | ✅ |

---

## 2. 공개 API (일반 사용자)

### 2.1 채널
| 메서드 | 엔드포인트 | 설명 | 상태 |
|--------|-----------|------|------|
| GET | `/channels` | 공개 채널 목록 | ✅ |
| GET | `/channels/{uuid}` | 채널 상세 정보 | ✅ |
| GET | `/channels/{uuid}/profile` | 채널 프로필 이미지 | ✅ |
| GET | `/channels/{uuid}/platforms` | 채널의 플랫폼 목록 | ✅ |

### 2.2 비디오
| 메서드 | 엔드포인트 | 설명 | 상태 |
|--------|-----------|------|------|
| GET | `/videos` | 공개 비디오 목록 (페이징) | ✅ |
| GET | `/videos/{uuid}` | 비디오 상세 정보 | ✅ |
| GET | `/videos/{uuid}/thumbnail` | 비디오 썸네일 이미지 | ✅ |
| GET | `/videos/{uuid}/playlist.m3u8` | HLS 플레이리스트 | ✅ |
| GET | `/videos/{uuid}/{filename}` | HLS 세그먼트 파일 (segment_N.ts) | ✅ |

### 2.3 메타데이터
| 메서드 | 엔드포인트 | 설명 | 상태 |
|--------|-----------|------|------|
| GET | `/videos/{uuid}/chat` | 채팅 히스토리 (offsetStart, offsetEnd 파라미터 필수) | ✅ |
| GET | `/videos/{uuid}/viewer-histories` | 시청자 수 히스토리 | ❌ |
| GET | `/videos/{uuid}/title-histories` | 제목 변경 히스토리 | ❌ |
| GET | `/videos/{uuid}/category-histories` | 카테고리 변경 히스토리 | ❌ |

---

## 3. 사용자 API 🔒

### 3.1 시청 기록
| 메서드 | 엔드포인트 | 설명 | 상태 |
|--------|-----------|------|------|
| GET | `/histories` | 시청 기록 목록 (페이징) | ❌ |
| GET | `/videos/{uuid}/watch-history` | 개별 영상 시청 기록 조회 | ❌ |
| POST | `/videos/{uuid}/watch-history` | 시청 위치 저장 (UPSERT) | ❌ |
| DELETE | `/histories/{videoUuid}` | 개별 시청 기록 삭제 | ❌ |
| DELETE | `/histories` | 전체 시청 기록 삭제 | ❌ |

### 3.2 마이페이지
| 메서드 | 엔드포인트 | 설명 | 상태 |
|--------|-----------|------|------|
| GET | `/users/me` | 내 정보 조회 | ✅ |
| PUT | `/users/me` | 내 정보 수정 | ❌ |
| PUT | `/users/me/password` | 비밀번호 변경 | ❌ |

---

## 4. 관리자 API 👑

### 4.1 채널 관리
| 메서드 | 엔드포인트 | 설명 | 상태 |
|--------|-----------|------|------|
| GET | `/admin/channels` | 전체 채널 목록 | ✅ |
| GET | `/admin/channels/{id}` | 채널 상세 조회 | ✅ |
| POST | `/admin/channels` | 채널 생성 | ✅ |
| PUT | `/admin/channels/{id}` | 채널 수정 | ✅ |
| DELETE | `/admin/channels/{id}` | 채널 삭제 | ✅ |

### 4.2 채널 플랫폼 관리
| 메서드 | 엔드포인트 | 설명 | 상태 |
|--------|-----------|------|------|
| GET | `/admin/channel-platforms` | 플랫폼 목록 | ✅ |
| GET | `/admin/channel-platforms/{id}` | 플랫폼 상세 조회 | ✅ |
| POST | `/admin/channel-platforms` | 플랫폼 추가 | ✅ |
| PUT | `/admin/channel-platforms/{id}` | 플랫폼 수정 | ✅ |
| DELETE | `/admin/channel-platforms/{id}` | 플랫폼 삭제 | ✅ |

### 4.3 녹화 스케줄 관리
| 메서드 | 엔드포인트 | 설명 | 상태 |
|--------|-----------|------|------|
| GET | `/admin/record-schedules` | 스케줄 목록 | ✅ |
| GET | `/admin/record-schedules/{id}` | 스케줄 상세 조회 | ✅ |
| POST | `/admin/record-schedules` | 스케줄 생성 | ✅ |
| PUT | `/admin/record-schedules/{id}` | 스케줄 수정 | ✅ |
| DELETE | `/admin/record-schedules/{id}` | 스케줄 삭제 | ✅ |

### 4.4 녹화 기록 관리
| 메서드 | 엔드포인트 | 설명 | 상태 |
|--------|-----------|------|------|
| GET | `/admin/records` | 녹화 기록 목록 | ✅ |
| GET | `/admin/records/{id}` | 녹화 기록 상세 | ✅ |
| POST | `/admin/records/{id}/cancel` | 녹화 취소 | ✅ |

### 4.5 비디오 관리
| 메서드 | 엔드포인트 | 설명 | 상태 |
|--------|-----------|------|------|
| GET | `/admin/videos` | 전체 비디오 목록 | ✅ |
| GET | `/admin/videos/{id}` | 비디오 상세 조회 | ✅ |
| PUT | `/admin/videos/{id}` | 비디오 수정 (제목, 공개범위) | ✅ |
| DELETE | `/admin/videos/{id}` | 비디오 삭제 | ✅ |

### 4.6 사용자 관리
| 메서드 | 엔드포인트 | 설명 | 상태 |
|--------|-----------|------|------|
| GET | `/admin/users` | 사용자 목록 | ✅ |
| GET | `/admin/users/{id}` | 사용자 상세 조회 | ✅ |
| PUT | `/admin/users/{id}/role` | 역할 변경 (ADMIN ↔ USER) | ✅ |
| DELETE | `/admin/users/{id}` | 사용자 삭제 | ✅ |

### 4.7 시스템 설정
| 메서드 | 엔드포인트 | 설명 | 상태 |
|--------|-----------|------|------|
| GET | `/admin/settings/global` | 전역 설정 조회 | ❌ |
| PUT | `/admin/settings/global` | 전역 설정 수정 | ❌ |
| GET | `/admin/settings/channels/{id}` | 채널별 설정 조회 | ❌ |
| PUT | `/admin/settings/channels/{id}` | 채널별 설정 수정 | ❌ |

### 4.8 대시보드
| 메서드 | 엔드포인트 | 설명 | 상태 |
|--------|-----------|------|------|
| GET | `/admin/dashboard/stats` | 대시보드 통계 (채널 수, 동영상 수, 녹화 시간, 스토리지) | ✅ |
| GET | `/admin/dashboard/video-histories` | 동영상 히스토리 (최근 30일간 누적 통계) | ✅ |

---

## 구현 현황 요약

### ✅ 구현 완료 (45개)
- 인증 API: 4개
- 공개 채널 API: 4개
- 공개 비디오 API: 5개
- 공개 메타데이터 API: 1개 (채팅 히스토리)
- 사용자 API: 1개 (내 정보 조회)
- 채널 관리: 5개
- 채널 플랫폼 관리: 5개
- 녹화 스케줄 관리: 5개
- 녹화 기록 관리: 3개
- 비디오 관리: 4개
- 사용자 관리: 4개
- 대시보드: 2개 (통계, 동영상 히스토리)
- JWT 기반 인증/인가 시스템 구현 완료

### ❌ 미구현 (14개)
- 메타데이터 API: 3개
- 시청 기록 API: 5개
- 사용자 API: 2개
- 관리자 설정 관리: 4개

### 전체 구현률
**45 / 59 = 76%**
