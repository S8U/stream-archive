# 비디오 플레이어 유튜브 스타일 인터랙션 피드백 오버레이

## Context

`frontend/src/app/(app)/videos/[uuid]/video-player.tsx`의 비디오 플레이어는 현재 재생/일시정지·5초 시킹 동작이 키보드/클릭으로 즉시 일어나지만 시각 피드백이 없어, 사용자가 자기 입력이 반영됐는지 즉시 알기 어렵다. (예: 일시정지 키를 눌렀는데 컨트롤이 숨겨져 있으면 변화를 인지하기까지 미세한 지연이 있음)

유튜브식 즉시 피드백 패턴을 도입한다.

- 재생/일시정지 시 화면 정중앙에 큰 ▶ / ⏸ 아이콘이 잠깐 떴다 사라짐
- ◀ 5초 뒤로(ArrowLeft) 시 화면 좌측에 `-5초` 표시, ▶ 5초 앞으로(ArrowRight) 시 우측에 `+5초` 표시
- **사라지기 전에 같은 방향이 연속으로 눌리면 합산해서 표시** (예: 뒤로 3번 → `-5` → `-10` → `-15`). 반대 방향이 눌리면 새 인디케이터가 그 자리(반대편)에 새 값으로 시작.

기존 코드베이스의 `volumeIndicator` 패턴(`video-player.tsx:121-122`, `:249-255`)과 동일한 "state + setTimeout" 방식이 이미 자리잡혀 있어, 그 패턴을 두 번 더 복제해 일관성을 유지하면서 추가한다. `framer-motion` 등 신규 의존성은 추가하지 않는다.

---

## 변경 대상 파일

- `frontend/src/app/(app)/videos/[uuid]/video-player.tsx` (단일 파일, 핵심 변경)
- `frontend/src/app/globals.css` (페이드아웃 keyframes 한 블록 추가)

이 외 파일은 수정하지 않는다.

---

## 구현 설계

### 1. lucide 아이콘 추가 import (`video-player.tsx:5-14`)

`Play`, `Pause`는 이미 import되어 있어 그대로 사용. 시킹 화살표는 가독성을 위해 직관적인 아이콘이 필요하므로 추가:

```ts
import { Play, Pause, Volume2, VolumeX, Maximize, Minimize, PanelRight, Loader2,
         RotateCcw, RotateCw } from 'lucide-react';
```

`RotateCcw`(반시계 화살표) / `RotateCw`(시계 화살표)는 유튜브 모바일 앱이 쓰는 "되감기 5초/앞으로 5초" 시각적 메타포와 가장 가깝다. (`FastForward`/`Rewind`의 두 개 삼각형은 "다음 챕터 이동" 인상이 있어 5초 시킹과는 의미상 거리가 있음)

### 2. 상수 추가 (`video-player.tsx:31-35` 근처)

```ts
const PLAY_PAUSE_INDICATOR_MS = 500;  // 짧고 명료
const SEEK_INDICATOR_MS = 800;        // 합산 윈도우
```

### 3. 새 state / ref 추가 (`video-player.tsx:121-122` 인근, `volumeIndicator` 바로 아래)

```ts
// 재생/일시정지 중앙 인디케이터
const [playPauseIndicator, setPlayPauseIndicator] = useState<{
    kind: 'play' | 'pause';
    nonce: number;  // 같은 종류 연속 트리거 시 애니메이션 재시작용
} | null>(null);
const playPauseIndicatorTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

// 시킹 좌/우 인디케이터 (방향 + 누적 초)
const [seekIndicator, setSeekIndicator] = useState<{
    direction: 'backward' | 'forward';
    seconds: number;
} | null>(null);
const seekIndicatorTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
```

### 4. 트리거 헬퍼 두 개 (`video-player.tsx:248-255` `showVolumeIndicator` 아래)

```ts
const showPlayPauseIndicator = useCallback((kind: 'play' | 'pause') => {
    setPlayPauseIndicator({ kind, nonce: Date.now() });
    if (playPauseIndicatorTimerRef.current) clearTimeout(playPauseIndicatorTimerRef.current);
    playPauseIndicatorTimerRef.current = setTimeout(() => {
        setPlayPauseIndicator(null);
    }, PLAY_PAUSE_INDICATOR_MS);
}, []);

const showSeekIndicator = useCallback((direction: 'backward' | 'forward') => {
    setSeekIndicator((prev) => {
        const sameDirection = prev && prev.direction === direction;
        const nextSeconds = (sameDirection ? prev!.seconds : 0) + SEEK_STEP_SEC;
        return { direction, seconds: nextSeconds };
    });
    if (seekIndicatorTimerRef.current) clearTimeout(seekIndicatorTimerRef.current);
    seekIndicatorTimerRef.current = setTimeout(() => {
        setSeekIndicator(null);
    }, SEEK_INDICATOR_MS);
}, []);
```

핵심 동작:
- `setSeekIndicator`의 functional updater에서 이전 상태(`prev`)를 보고 같은 방향이면 누적, 다르면 리셋. **타이머가 살아있는 동안만 누적**되는 셈(타임아웃 후 `null`이 되어 다시 0부터).
- 같은 방향 연속 입력 시 매번 타이머 재시작 → 사용자가 빠르게 누르는 동안에는 계속 살아있음.
- 반대 방향 입력은 즉시 자리/숫자가 바뀐 새 인디케이터로 대체.
- `playPauseIndicator`는 `nonce`를 두어 같은 kind가 연속으로 트리거돼도 React `key`가 바뀌어 애니메이션이 처음부터 재생되도록 함.

### 5. 비디오 이벤트에서 재생/일시정지 인디케이터 트리거 (`video-player.tsx:334-342`)

`handlePlay` / `handlePause`에 한 줄씩 추가:

```ts
const handlePlay = () => {
    setIsPlaying(true);
    showControls();
    showPlayPauseIndicator('play');   // 추가
};
const handlePause = () => {
    setIsPlaying(false);
    setIsControlsVisible(true);
    if (hideTimerRef.current) clearTimeout(hideTimerRef.current);
    showPlayPauseIndicator('pause');  // 추가
};
```

이 위치에 두는 것이 핵심: 키보드 Space, 비디오 본체 클릭, 컨트롤 바 버튼, 코드 어디서 발생한 토글이든 video element의 `play`/`pause` 이벤트가 단일 진실 공급원이므로 트리거가 한 곳에 모인다. `togglePlay` 안에 직접 넣으면 다른 경로에서 누락된다.

`useEffect` 의존성 배열(`video-player.tsx:382`)에 `showPlayPauseIndicator` 추가.

> **초기 마운트 시 깜빡임 가능성**: 코드상 자동재생 호출은 없어 `play` 이벤트가 자동으로 발생하지 않지만, 검증 단계에서 깜빡임이 보이면 `hasRestoredPositionRef`처럼 첫 트리거를 한 번 무시하는 가드를 추가. 1차 구현은 가드 없이 진행.

### 6. 시킹 키 핸들러에서 시킹 인디케이터 트리거 (`video-player.tsx:407-416`)

```ts
case 'ArrowLeft':
    e.preventDefault();
    seekTo(video.currentTime - SEEK_STEP_SEC);
    showControls();
    showSeekIndicator('backward');   // 추가
    break;
case 'ArrowRight':
    e.preventDefault();
    seekTo(video.currentTime + SEEK_STEP_SEC);
    showControls();
    showSeekIndicator('forward');    // 추가
    break;
```

`useEffect` 의존성 배열(`video-player.tsx:463`)에 `showSeekIndicator` 추가.

> **시킹 트리거 경로**: 5초 시킹은 **키보드 ArrowLeft/Right만** 발생한다. 타임라인 클릭/드래그는 절대 위치 시킹이라 ±5초 합산과 의미가 다르므로 인디케이터 대상 아님. 따라서 키 핸들러 두 case에만 추가하면 충분.

### 7. 페이드아웃 keyframes 추가 (`frontend/src/app/globals.css`)

파일 하단에 짧은 keyframes 한 블록을 추가:

```css
@keyframes videoPlayerFadeOut {
    0%   { opacity: 1; transform: scale(1); }
    50%  { opacity: 1; transform: scale(1); }
    100% { opacity: 0; transform: scale(1.1); }
}
```

(앞 50%까지 또렷하게 보이고 후반에 페이드 + 살짝 확대 — 유튜브 인디케이터 톤과 비슷)

### 8. 오버레이 JSX 추가 (`video-player.tsx:638` `volumeIndicator` 블록 직후)

`containerRef` div의 직속 자식, 컨트롤 오버레이(`video-player.tsx:640~`) 위쪽에 두 개의 오버레이를 삽입. 모두 `pointer-events-none`으로 클릭 토글/풀스크린 동작을 막지 않음.

```tsx
{/* 재생/일시정지 중앙 인디케이터 (유튜브 스타일) */}
{playPauseIndicator !== null && (
    <div
        key={`pp-${playPauseIndicator.kind}-${playPauseIndicator.nonce}`}
        className="absolute inset-0 flex items-center justify-center pointer-events-none"
        style={{ animation: `videoPlayerFadeOut ${PLAY_PAUSE_INDICATOR_MS}ms ease-out forwards` }}
    >
        <div className="bg-black/60 rounded-full p-5">
            {playPauseIndicator.kind === 'play' ? (
                <Play size={48} className="text-white" fill="currentColor" />
            ) : (
                <Pause size={48} className="text-white" fill="currentColor" />
            )}
        </div>
    </div>
)}

{/* 시킹 ±N초 인디케이터 (유튜브 스타일) */}
{seekIndicator !== null && (
    <div
        key={`seek-${seekIndicator.direction}-${seekIndicator.seconds}`}
        className={`absolute top-1/2 -translate-y-1/2 pointer-events-none ${
            seekIndicator.direction === 'backward' ? 'left-[15%]' : 'right-[15%]'
        }`}
        style={{ animation: `videoPlayerFadeOut ${SEEK_INDICATOR_MS}ms ease-out forwards` }}
    >
        <div className="flex flex-col items-center gap-1 bg-black/60 rounded-full px-5 py-4">
            {seekIndicator.direction === 'backward' ? (
                <RotateCcw size={32} className="text-white" />
            ) : (
                <RotateCw size={32} className="text-white" />
            )}
            <span className="text-white text-sm font-medium tabular-nums">
                {seekIndicator.direction === 'backward' ? '-' : '+'}
                {seekIndicator.seconds}초
            </span>
        </div>
    </div>
)}
```

#### 합산 시 애니메이션 리셋

React `key`에 누적값(`seconds`) 또는 `nonce`를 포함시켜 매 트리거마다 컴포넌트가 새 노드로 교체되게 함 → CSS 애니메이션이 처음부터 재생됨. (단순히 state만 바꾸면 같은 노드라 애니메이션이 진행 중간 상태로 남음)

#### 정수 `setTimeout` 값을 인라인 style에 보간하는 이유

PostCSS / Tailwind의 임의값 클래스(`animate-[...]`)는 빌드 타임에 결정되어야 해서 상수 보간이 깔끔하지 않음. 인라인 style은 런타임 보간이 자연스러우며, `pointer-events-none`/위치 클래스는 그대로 Tailwind를 쓸 수 있어 가독성도 유지됨.

### 9. cleanup (선택)

언마운트 시 두 타이머 정리. 기존 `volumeIndicatorTimerRef`도 cleanup이 없는 패턴이라 강제는 아니지만, 일관성을 위해 1차 구현은 기존 패턴을 따라 생략. 메모리/타이머 누수 우려가 보이면 별도 effect로 cleanup 추가.

---

## 재사용하는 기존 함수/패턴

- **`volumeIndicator` 패턴** (`video-player.tsx:121-122`, `:249-255`): state + setTimeout + ref 보관. `playPauseIndicator`/`seekIndicator`가 정확히 같은 구조를 따름.
- **상수 `SEEK_STEP_SEC = 5`** (`video-player.tsx:32`): 합산 단위로 그대로 재사용.
- **버퍼링 스피너 마운트 위치** (`video-player.tsx:624-629`): 같은 부모 컨테이너에 같은 `absolute inset-0 ... pointer-events-none` 패턴으로 새 오버레이 배치.
- **컨테이너 `containerRef`** (`video-player.tsx:97-98`, `:602`): `relative` 부모로 모든 오버레이의 좌표 기준. 풀스크린/와이드/일반 모드 모두에서 video와 같은 박스를 차지하므로 풀스크린에서도 자동으로 가운데에 뜸.
- **lucide-react 아이콘 import**: 기존 import 라인에 `RotateCcw`, `RotateCw` 두 개 추가.

신규 파일/컴포넌트/유틸은 만들지 않는다.

---

## 검증 방법

UI 인터랙션이라 자동화 어려움 — 실제 브라우저에서 수동 확인.

### 1. 개발 서버 기동

```bash
cd frontend
pnpm run dev
```

브라우저에서 비디오 페이지(`/videos/<uuid>`)를 연다.

### 2. 재생/일시정지 인디케이터

- Space 또는 K 누름 → 화면 정중앙에 ⏸(또는 ▶) 큰 아이콘이 떴다가 ~0.5초 페이드아웃
- 컨트롤 바의 재생 버튼 클릭 → 동일 동작
- 비디오 본체 클릭(컨트롤 바 영역 외) → 동일 동작
- 더블클릭으로 풀스크린 진입 → 인디케이터가 풀스크린에서도 화면 중앙에 정확히 뜨는지 (containerRef 기준이라 자동으로 됨)

### 3. 시킹 인디케이터 (단일)

- ArrowLeft → 화면 좌측 1/5 지점에 `-5초` 표시 + 반시계 화살표, ~0.8초 후 페이드아웃
- ArrowRight → 우측 1/5 지점에 `+5초`

### 4. 시킹 인디케이터 (합산) — 핵심 시나리오

- ArrowLeft를 0.5초 간격으로 3번 빠르게 → `-5초` → `-10초` → `-15초`로 숫자만 갱신되며 인디케이터는 좌측에 계속 보임 (애니메이션은 매번 리셋)
- 마지막 누름 후 0.8초 지나면 페이드아웃
- ArrowRight를 같은 방식으로 → `+5초` → `+10초` → `+15초`
- ArrowLeft 두 번(`-10초` 표시 중) → 곧바로 ArrowRight → 우측에 `+5초`로 새로 시작 (좌측 인디케이터는 사라짐)

### 5. 클릭/풀스크린 동작이 망가지지 않는지 (`pointer-events-none` 검증)

- 인디케이터가 떠 있는 동안 비디오 본체를 클릭 → 재생/일시정지 토글이 정상 작동
- 인디케이터가 떠 있는 동안 더블클릭 → 풀스크린 토글 정상 작동

### 6. input/textarea 포커스 시

- 채팅 검색 등 input에 포커스가 있을 때 화살표 키 → 인디케이터 안 뜨고 시킹도 안 됨 (기존 가드, `video-player.tsx:387-394`)

### 7. 타입/빌드 검증

```bash
cd frontend
pnpm run build
```

타입스크립트 에러 0개 확인.

---

## 잠재 이슈 및 대응

- **초기 자동재생/메타데이터 이벤트로 깜빡임**: `handlePlay` 첫 호출에서 인디케이터가 의도치 않게 뜬다면, "첫 play 이벤트는 무시" 플래그를 추가. (1차 구현 후 검증)
- **빠른 연타 시 잔상**: `seekIndicator`의 `key`에 `seconds`를 포함시켜 매번 리마운트 → 애니메이션 처음부터. 검증 단계에서 충분한지 확인.
- **풀스크린 좌/우 위치(`left-[15%]`)가 너무 안쪽/바깥쪽**: 16:9에서 적절. 검증 후 조정 가능. (참고: 유튜브는 약 25% 지점)
- **모바일 터치**: 더블탭으로 시킹하는 패턴은 별도 작업이며 본 변경에는 포함하지 않음. 키보드/클릭만 대상.
