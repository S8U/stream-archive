# CHZZK 플랫폼 API

비공식 API.

## 채널 조회

### Endpoint

```text
GET https://api.chzzk.naver.com/service/v1/channels/{channelId}
```

### Request

```bash
curl --http1.1 -L --compressed \
  -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36' \
  -H 'Accept: application/json, text/plain, */*' \
  -H 'Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7' \
  -H 'Origin: https://chzzk.naver.com' \
  -H 'Referer: https://chzzk.naver.com/' \
  'https://api.chzzk.naver.com/service/v1/channels/example-channel-id'
```

### Response

```json
{
  "code": 200,
  "message": null,
  "content": {
    "channelId": "example-channel-id",
    "channelName": "example-channel-name",
    "channelImageUrl": "https://example.com/channel-image.png",
    "verifiedMark": true,
    "channelType": "STREAMING",
    "channelDescription": "example channel description",
    "followerCount": 123456,
    "openLive": false,
    "subscriptionAvailability": true,
    "subscriptionPaymentAvailability": {
      "iapAvailability": false,
      "iabAvailability": true
    },
    "adMonetizationAvailability": true,
    "activatedChannelBadgeIds": [
      "example-badge-id"
    ],
    "paidProductSaleAllowed": false
  }
}
```

## 라이브 상세 조회

### Endpoint

```text
GET https://api.chzzk.naver.com/service/v2/channels/{channelId}/live-detail
```

### Request

```bash
curl --http1.1 -L --compressed \
  -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36' \
  -H 'Accept: application/json, text/plain, */*' \
  -H 'Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7' \
  -H 'Origin: https://chzzk.naver.com' \
  -H 'Referer: https://chzzk.naver.com/' \
  'https://api.chzzk.naver.com/service/v2/channels/example-channel-id/live-detail'
```

### Response

```json
{
  "code": 200,
  "message": null,
  "content": {
    "liveId": 12345678,
    "liveTitle": "example live title",
    "status": "CLOSE",
    "liveImageUrl": null,
    "defaultThumbnailImageUrl": null,
    "concurrentUserCount": 1234,
    "accumulateCount": 12345,
    "openDate": "2026-05-10 19:57:51",
    "closeDate": "2026-05-11 01:36:40",
    "adult": false,
    "tags": [
      "example-tag"
    ],
    "chatChannelId": "example-chat-channel-id",
    "categoryType": "GAME",
    "liveCategory": "example_category",
    "liveCategoryValue": "example category",
    "livePlaybackJson": "{\"meta\":{\"liveId\":\"12345678\"},\"live\":{\"status\":\"ENDED\"},\"media\":[]}",
    "channel": {
      "channelId": "example-channel-id",
      "channelName": "example-channel-name",
      "channelImageUrl": "https://example.com/channel-image.png",
      "verifiedMark": true
    }
  }
}
```

## 채팅 액세스 토큰 조회

### Endpoint

```text
GET https://comm-api.game.naver.com/nng_main/v1/chats/access-token?channelId={chatChannelId}&chatType=STREAMING
```

### Request

```bash
curl --http1.1 -L --compressed \
  -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36' \
  -H 'Accept: application/json, text/plain, */*' \
  -H 'Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7' \
  -H 'Origin: https://chzzk.naver.com' \
  -H 'Referer: https://chzzk.naver.com/' \
  'https://comm-api.game.naver.com/nng_main/v1/chats/access-token?channelId=example-chat-channel-id&chatType=STREAMING'
```

### Response

```json
{
  "code": 200,
  "message": null,
  "content": {
    "accessToken": "example-access-token",
    "temporaryRestrict": null,
    "realNameAuth": false,
    "extraToken": "example-extra-token"
  }
}
```

## 채팅 WebSocket

공개 문서가 없다.
아래 내용은 2026-10-14 공개 라이브 채팅을 READ 권한으로 받아 확인한 것과 비공식 라이브러리([kimcore/chzzk](https://github.com/kimcore/chzzk), [chzzkpy](https://github.com/gunyu1019/chzzkpy))를 대조한 것이다.
받은 프레임 원본은 동영상별로 보관한다([채팅 원본 보관](../features/chat-raw.md)).

### Endpoint

```text
wss://kr-ss{serverId}.chat.naver.com/chat
```

`serverId`는 `chatChannelId` 각 문자 코드의 합을 9로 나눈 나머지에 1을 더한 값이다.

### 명령 (`cmd`)

| 값 | 이름 | 방향 | 설명 |
|---|---|---|---|
| `0` | PING | 받음 | 받으면 PONG을 보낸다 |
| `10000` | PONG | 보냄 | |
| `100` | REQUEST_CONNECT | 보냄 | 채팅방 접속. `bdy.accTkn`에 채팅 액세스 토큰, `bdy.auth`에 `READ` |
| `10100` | RESPONSE_CONNECT | 받음 | 접속 응답 |
| `93101` | CHAT | 받음 | 채팅. `bdy`는 메시지 배열 |
| `93102` | DONATION | 받음 | 후원 채팅. `bdy`는 메시지 배열 |

### 메시지 (`bdy` 원소)

```json
{
  "msg": "채팅 내용 {:d_1:}",
  "msgTypeCode": 1,
  "profile": "{\"nickname\":\"example-nickname\",\"userRoleCode\":\"common_user\",...}",
  "extras": "{\"emojis\":{\"d_1\":\"https://example.com/emoji.png\"},...}",
  "msgTime": 1760436000000
}
```

`profile`과 `extras`는 JSON 문자열이라 한 번 더 파싱한다.

| 필드 | 설명 |
|---|---|
| `msgTypeCode` | `1` 채팅, `10` 후원 |
| `profile.nickname` | 닉네임 |
| `profile.userRoleCode` | 작성자 역할 (아래 표) |
| `extras.emojis` | 이모티콘 ID와 이미지 URL. 메시지에는 `{:ID:}`로 들어 있다 |

`profile.userRoleCode` 값은 다음과 같다.
역할이 있으면 `profile.badge.imageUrl`과 `profile.title`(이름·색)도 함께 온다.

| 값 | 화면 표기 | 실측 | 우리 역할 |
|---|---|---|---|
| `common_user` | 없음 | 확인 | 없음 |
| `streamer` | 스트리머 | 확인 | `STREAMER` |
| `streaming_chat_manager` | 채팅 운영자 | 확인 | `MANAGER` |
| `streaming_channel_manager` | 채널 관리자 | 확인 | `MANAGER` |
| `streaming_settlement_manager` | | 미확인 (라이브러리에만 있음) | `MANAGER` |
| `manager` | | 미확인 (라이브러리에만 있음) | `MANAGER` |

### 후원 (`cmd` 93102, `msgTypeCode` 10)

| 필드 | 설명 |
|---|---|
| `extras.payAmount` | 후원 금액 (치즈) |
| `extras.payType` | 결제 수단. 실측은 `CURRENCY`만 확인 |
| `extras.isAnonymous` | 익명 후원 여부. 익명이면 `profile`이 `null`이다 |
| `extras.donationType` | 후원 종류. 실측은 `CHAT`, `VIDEO`만 확인 |

같은 명령으로 다른 `msgTypeCode`도 온다고 라이브러리에 나와 있다(`11` 구독, `12` 구독권 선물, `30` 시스템 메시지).
실측에서는 확인하지 못했다.
