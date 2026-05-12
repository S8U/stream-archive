# SOOP 플랫폼 API

비공식 API.

## 스테이션 조회

### Endpoint

```text
GET https://chapi.sooplive.co.kr/api/{userId}/station
```

### Request

```bash
curl --http1.1 -L --compressed \
  -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36' \
  -H 'Accept: application/json, text/plain, */*' \
  -H 'Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7' \
  'https://chapi.sooplive.co.kr/api/example-user-id/station'
```

### Response

```json
{
  "profile_image": "//profile.img.sooplive.co.kr/LOGO/ex/example-user-id/example-user-id.jpg",
  "station": {
    "station_no": 1234567,
    "user_id": "example-user-id",
    "user_nick": "example-user-nick",
    "station_name": "example station name",
    "station_title": "example station title",
    "grade": 0,
    "broad_start": "2026-05-12 20:05:02",
    "total_broad_time": 12345678
  },
  "broad": null
}
```

## 라이브 상세 조회

### Endpoint

```text
POST https://live.sooplive.co.kr/afreeca/player_live_api.php?bjid={userId}
```

### Request

```bash
curl --http1.1 -L --compressed \
  -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36' \
  -H 'Accept: application/json, text/plain, */*' \
  -H 'Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  'https://live.sooplive.co.kr/afreeca/player_live_api.php?bjid=example-user-id' \
  --data 'bid=example-user-id&type=live&pwd=&player_type=html5&stream_type=common&quality=HD&mode=landing&from_api=0&is_revive=false'
```

### Response

오프라인 상태 예시:

```json
{
  "CHANNEL": {
    "geo_cc": "KR",
    "geo_rc": "11",
    "acpt_lang": "ko_KR",
    "svc_lang": "ko_KR",
    "ISSP": 0,
    "RESULT": 0,
    "GDPR": false
  }
}
```

라이브 상태 예시:

```json
{
  "CHANNEL": {
    "RESULT": 1,
    "BNO": "123456789",
    "BJID": "example-user-id",
    "BJNICK": "example-user-nick",
    "TITLE": "example live title",
    "CATE": "example category",
    "BTIME": 1234,
    "RESOLUTION": "1920x1080",
    "BPS": "8000"
  }
}
```
