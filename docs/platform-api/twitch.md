# Twitch 플랫폼 API

공식 Helix API.

## OAuth 토큰 발급

### Endpoint

```text
POST https://id.twitch.tv/oauth2/token
```

### Request

```bash
curl -L \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  'https://id.twitch.tv/oauth2/token' \
  --data-urlencode 'client_id=example-client-id' \
  --data-urlencode 'client_secret=example-client-secret' \
  --data-urlencode 'grant_type=client_credentials'
```

### Response

```json
{
  "access_token": "example-access-token",
  "expires_in": 5011271,
  "token_type": "bearer"
}
```

## 유저 조회

### Endpoint

```text
GET https://api.twitch.tv/helix/users?login={login}
```

### Request

```bash
curl -L \
  -H 'Authorization: Bearer example-oauth-token' \
  -H 'Client-Id: example-client-id' \
  'https://api.twitch.tv/helix/users?login=example-login'
```

### Response

```json
{
  "data": [
    {
      "id": "123456789",
      "login": "example-login",
      "display_name": "example-display-name",
      "type": "",
      "broadcaster_type": "",
      "description": "example channel description",
      "profile_image_url": "https://static-cdn.jtvnw.net/jtv_user_pictures/example-profile.png",
      "offline_image_url": "https://static-cdn.jtvnw.net/jtv_user_pictures/example-offline.png",
      "view_count": 0,
      "created_at": "2016-10-22T23:47:35Z"
    }
  ]
}
```

## 스트림 조회

### Endpoint

```text
GET https://api.twitch.tv/helix/streams?user_login={login}
```

### Request

```bash
curl -L \
  -H 'Authorization: Bearer example-oauth-token' \
  -H 'Client-Id: example-client-id' \
  'https://api.twitch.tv/helix/streams?user_login=example-login'
```

### Response

오프라인 상태 예시:

```json
{
  "data": [],
  "pagination": {}
}
```

라이브 상태 예시:

```json
{
  "data": [
    {
      "id": "1234567890",
      "user_id": "123456789",
      "user_login": "example-login",
      "user_name": "example-display-name",
      "game_id": "12345",
      "game_name": "example game",
      "type": "live",
      "title": "example live title",
      "viewer_count": 1234,
      "started_at": "2026-05-13T10:00:00Z",
      "language": "ko",
      "thumbnail_url": "https://static-cdn.jtvnw.net/previews-ttv/live_user_example-login-{width}x{height}.jpg",
      "tag_ids": [],
      "tags": [
        "example-tag"
      ],
      "is_mature": false
    }
  ],
  "pagination": {
    "cursor": "example-cursor"
  }
}
```

## 채팅 IRC

공식 문서: [Twitch IRC](https://dev.twitch.tv/docs/chat/irc/)

### Endpoint

```text
wss://irc-ws.chat.twitch.tv:443
```

`twitch.tv/tags`, `twitch.tv/commands` capability를 요청하고 익명 닉네임(`justinfan` + 숫자)으로 NICK만 보낸 뒤 채널에 JOIN한다.
받은 프레임 원본은 동영상별로 보관한다([채팅 원본 보관](../features/chat-raw.md)).

### 채팅 (`PRIVMSG`)

```text
@badges=moderator/1;display-name=example-user;emotes=25:0-4;mod=1 :example-user!example-user@example-user.tmi.twitch.tv PRIVMSG #example-channel :Kappa
```

| 태그 | 설명 |
|---|---|
| `display-name` | 표시 이름 |
| `emotes` | 이모트 ID와 메시지 안 위치(코드포인트 기준) |
| `badges` | `배지/버전`을 쉼표로 이은 목록. `broadcaster`, `moderator` 등 |
| `mod` | 모더레이터면 `1` |

작성자 역할은 다음과 같이 정한다.

| 조건 | 우리 역할 |
|---|---|
| `badges`에 `broadcaster`가 있다 | `STREAMER` |
| `mod`가 `1`이다 | `MANAGER` |
| 그 밖 | 없음 |
