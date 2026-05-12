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
