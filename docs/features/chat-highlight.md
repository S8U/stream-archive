# 채팅 역할·후원 표시

## 상태

- 구현 완료

## 목적

다시보기 채팅이 빠르게 흘러가도 스트리머와 매니저의 채팅이 눈에 띄게 한다.

## 역할

플랫폼마다 역할을 나타내는 방식이 달라서 공통 역할(`PlatformChatUserRole`)로 바꿔 채팅 행에 저장한다.
화면은 플랫폼과 상관없이 공통 역할만 보고 표시한다.

| 플랫폼 | 원본 | `STREAMER` | `MANAGER` |
|---|---|---|---|
| 치지직 | `profile.userRoleCode` | `streamer` | `streaming_chat_manager` 등 ([치지직 문서](../platform-api/chzzk.md)) |
| 트위치 | IRC `badges`, `mod` 태그 | `badges`에 `broadcaster` | `mod=1` ([트위치 문서](../platform-api/twitch.md)) |
| SOOP, 유튜브 | | 미지원 | 미지원 |

모르는 역할 값은 일반 시청자로 본다.

이 기능 전에 저장된 채팅에는 역할이 없다.
[채팅 원본](chat-raw.md)이 남아 있는 녹화는 원본을 다시 파싱해 채울 수 있다.

## 화면

- 역할이 있는 채팅은 둥근 회색 배경으로 구분한다.
- 닉네임 앞에 역할 아이콘(스트리머는 왕관, 매니저는 렌치)을 두고, 마우스를 올리면 역할 이름을 보여준다.
- 무채색 방침에 따라 악센트 색은 쓰지 않는다.

## 저장

`video_chat_histories.user_role`에 저장한다. 값이 없으면 일반 시청자다.

운영 테이블은 행이 많아서 nullable 컬럼으로 추가하고 인덱스는 두지 않는다.
`ddl-auto: update`는 없는 컬럼만 추가하고 기존 ENUM 정의는 바꾸지 않는다.
역할 값을 추가하면 운영 DB에 `ALTER TABLE`을 직접 실행해야 한다.

## 관련 코드

- `ChzzkChatUserRoleCode`: 치지직 역할 코드와 공통 역할의 대응표
- `TwitchChatWebSocketHandler.getUserRole`: 트위치 태그로 공통 역할을 구한다.
- `chat-history.tsx`: 역할 채팅 강조
