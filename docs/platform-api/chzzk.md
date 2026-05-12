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
