// 백엔드 API 주소. API 호출(axios-instance.ts)과 토큰 갱신(proxy.ts)이 함께 쓴다
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
