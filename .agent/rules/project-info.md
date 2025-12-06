---
trigger: always_on
---

반드시 먼저 @docs의 문서들을 읽어 현재 프로젝트에 대해 이해해줘
모든 생각과 답변 및 계획은 한국어로해줘

## 코딩 스타일
- 기본적으로 Google Style Guide를 따라줘
- 항상 기존 코드들을 확인하고, 기존 코드에 맞는 스타일로 코드를 작성해줘
- 백엔드를 수정할 때는 @AdminChannelController, @ChannelService를 직접 읽고, 해당 스타일로 코드를 작성해줘
- 프론트엔드를 수정할 때는 관리자쪽은 @admin/channel-platforms.tsx를 직접 읽고, 해당 스타일로 코드를 작성해줘
- 주석은 항상 한글로 작성해줘

## Frontend
- npm 대신 pnpm을 사용해줘
- API 수정 후 프론트엔드에서 해당 API를 사용하려면 `pnpm run orval`을 실행해줘
- CSR일 경우, orval로 생성된 React Query를 사용해줘