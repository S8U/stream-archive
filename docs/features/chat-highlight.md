# 채팅 역할·후원 표시

## 상태

- 구현 완료

## 목적

다시보기 채팅이 빠르게 흘러가도 스트리머와 매니저의 채팅, 후원이 눈에 띄게 한다.

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

## 후원

치지직 후원을 채팅 목록에 후원 카드로 보여준다. 다른 플랫폼은 아직 지원하지 않는다.

- 치지직은 후원을 채팅과 다른 명령(`93102`)으로 보낸다. 그중 `msgTypeCode`가 `10`인 것만 후원으로 저장한다.
- 같은 명령으로 오는 구독 알림 등은 아직 저장하지 않는다. [채팅 원본](chat-raw.md)에는 남아 있다.
- 금액은 치즈 단위 그대로 저장한다.
- 익명 후원은 프로필이 오지 않아 닉네임을 "익명의 후원자"로 저장하고 역할은 남기지 않는다.
- 후원도 채팅 행이라 채팅 분석(구간별 채팅 수, 키워드)에 함께 들어간다.
- 영상 후원(`donationType` `VIDEO`)도 같은 후원으로 저장한다. 종류는 원본에만 남는다.

## 화면

- 역할이 있는 채팅은 둥근 회색 배경으로 구분한다.
- 닉네임 앞에 역할 아이콘(스트리머는 왕관, 매니저는 렌치)을 두고, 마우스를 올리면 역할 이름을 보여준다.
- 후원은 치지직 채팅창과 같은 순서(닉네임 → 메시지 → 금액)로 쌓은 카드로 보여준다. 치지직의 금액대별 카드 색은 무채색 방침 때문에 따르지 않는다.
- 무채색 방침에 따라 악센트 색은 쓰지 않는다.

## 저장

| 컬럼 | 설명 |
|---|---|
| `video_chat_histories.message_type` | `CHAT` 또는 `DONATION`. 기존 행은 기본값 `CHAT` |
| `video_chat_histories.user_role` | `STREAMER` 또는 `MANAGER`. 없으면 일반 시청자 |
| `video_chat_histories.donation_amount` | 후원 금액. 후원이 아니면 null |

운영 테이블은 행이 많아서, 기존 행을 고치지 않고 바로 추가되도록 nullable이거나 기본값이 있는 컬럼으로 추가하고 인덱스는 두지 않는다.
`ddl-auto: update`는 없는 컬럼만 추가하고 기존 ENUM 정의는 바꾸지 않는다.
역할이나 메시지 종류 값을 추가하면 운영 DB에 `ALTER TABLE`을 직접 실행해야 한다.

## 관련 코드

- `ChzzkChatUserRoleCode`: 치지직 역할 코드와 공통 역할의 대응표
- `ChzzkChatWebSocketHandler.toDonationMessage`: 치지직 후원을 후원 채팅으로 바꾼다.
- `TwitchChatWebSocketHandler.getUserRole`: 트위치 태그로 공통 역할을 구한다.
- `chat-history.tsx`: 역할 채팅 강조와 후원 카드
