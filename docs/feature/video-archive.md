# 동영상 소장 기능

## Context

전체 방송을 자동 녹화하다 보니 스토리지가 빠르게 소진된다. 추후 도입할 "오래된 영상 자동 삭제" 스케줄러의 전제로, **개별 영상을 영구 보존(소장) 대상으로 표시**할 수 있는 수단이 필요하다. 자동 삭제 로직은 `is_archived = false` 인 영상만 대상으로 동작할 것이므로, 본 기능은 자동 삭제 기능의 선행 작업이다.

요구사항 정리:

1. 관리자 페이지 > 동영상 관리에서 **소장 여부 필터** 및 **소장/해제 토글**
2. 동영상 상세 페이지의 관리자 메뉴(`...`)에 **소장/해제 항목**
3. 동영상 목록(카드)·상세 페이지 제목 옆에 **소장 아이콘** (권한 무관 모두에게 노출)
4. 소장 처리한 user id, IP, 시간을 영상에 저장 + **별도 로그 테이블에 액션 이력 저장**

소장 설정 권한은 ADMIN만, 아이콘 표시는 비로그인을 포함한 모든 사용자에게 노출한다.

DB 스키마는 JPA `ddl-auto`로 반영하므로 별도 마이그레이션 SQL은 작성하지 않는다. `docs/schema.sql`은 참고용으로 함께 갱신.

---

## 변경 대상 파일

### Backend

- `backend/src/main/kotlin/com/github/s8u/streamarchive/entity/Video.kt` (필드/인덱스 추가)
- `backend/src/main/kotlin/com/github/s8u/streamarchive/entity/VideoArchiveLog.kt` (신규)
- `backend/src/main/kotlin/com/github/s8u/streamarchive/repository/VideoArchiveLogRepository.kt` (신규)
- `backend/src/main/kotlin/com/github/s8u/streamarchive/repository/VideoRepositoryCustom.kt` / `VideoRepositoryImpl.kt` (검색 조건 추가)
- `backend/src/main/kotlin/com/github/s8u/streamarchive/dto/VideoDto.kt` (요청/응답 DTO 확장)
- `backend/src/main/kotlin/com/github/s8u/streamarchive/service/VideoService.kt` (`setArchivedForAdmin` 추가)
- `backend/src/main/kotlin/com/github/s8u/streamarchive/controller/AdminVideoController.kt` (`PATCH /{id}/archive`)
- `docs/schema.sql` (참고용 동기화)

### Frontend (백엔드 변경 후 `pnpm orval` 재생성 선행)

- `frontend/src/app/admin/videos/page.tsx` (검색 체크박스, 테이블 토글, 제목 옆 아이콘)
- `frontend/src/components/video-info.tsx` (드롭다운 메뉴 항목, 제목 옆 아이콘)
- `frontend/src/components/video-card.tsx` (`isArchived` prop 추가, 제목 옆 아이콘)
- `frontend/src/components/admin/video-form-dialog.tsx` 는 **수정하지 않음** (소장은 별도 엔드포인트로 분리)
- 카드 호출부 (`isArchived` 전달): grep으로 사용처 탐색 후 일괄 수정

---

## 구현 설계

### 1. Video 엔티티 확장 (`entity/Video.kt`)

소장 상태 + 최초/최신 처리 정보를 영상 자체에 저장. 자동 삭제 쿼리(`WHERE is_archived = false AND created_at < ?`)를 빠르게 하기 위해 복합 인덱스도 같이 추가.

```kotlin
@Table(
    name = "videos",
    indexes = [
        // ... 기존 인덱스
        Index(name = "idx_videos_is_archived", columnList = "isArchived"),
        Index(name = "idx_videos_archived_created", columnList = "isArchived, createdAt")
    ]
)
```

필드 추가 (기존 audit 필드 패턴과 동일):

```kotlin
@Column(nullable = false)
@Comment("소장 여부")
var isArchived: Boolean = false,

@Comment("소장 처리 일시")
var archivedAt: LocalDateTime? = null,

@Comment("소장 처리한 사용자 ID")
var archivedBy: Long? = null,

@Column(length = 45)
@Comment("소장 처리 시 IP")
var archivedIp: String? = null,
```

해제 시 `archivedAt/By/Ip`는 모두 `null`로 비운다 (변경 이력은 `video_archive_logs`가 담당).

### 2. VideoArchiveLog 엔티티 (신규)

향후 다른 audit 로그도 같은 패턴으로 확장하기 위한 첫 사례. 영상별로 토글 액션을 1건씩 누적. 액션은 별도 enum 없이 `isArchived` boolean으로 표현 (해당 시점에 소장 상태로 전환되었는지 여부).

```kotlin
@Entity
@Table(
    name = "video_archive_logs",
    indexes = [
        Index(name = "idx_video_archive_logs_video_id", columnList = "videoId"),
        Index(name = "idx_video_archive_logs_created_at", columnList = "createdAt")
    ]
)
@EntityListeners(AuditingEntityListener::class)
@Comment("동영상 소장 이력")
class VideoArchiveLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("동영상 소장 이력 ID")
    val id: Long? = null,

    @Column(nullable = false)
    @Comment("동영상 ID")
    val videoId: Long,

    @Column(nullable = false)
    @Comment("소장 여부 (해당 시점 전환값)")
    val isArchived: Boolean,

    @Comment("액션 수행 사용자 ID")
    val actionBy: Long? = null,

    @Column(length = 45)
    @Comment("액션 수행 시 IP")
    val actionIp: String? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Comment("생성 일시")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

FK는 코딩 컨벤션(FK 미사용)에 따라 ID 컬럼만. `@SQLRestriction` 없음 — 이력은 hard delete 대상이 아님.

`repository/VideoArchiveLogRepository.kt`:

```kotlin
interface VideoArchiveLogRepository : JpaRepository<VideoArchiveLog, Long>
```

### 3. DTO 확장 (`dto/VideoDto.kt`)

```kotlin
data class AdminVideoSearchRequest(
    // ... 기존 필드
    val isArchived: Boolean? = null
)

data class AdminVideoArchiveRequest(
    val isArchived: Boolean
)
```

`AdminVideoResponse`에 소장 관련 필드 4개 추가, companion `from`에서 매핑:

```kotlin
data class AdminVideoResponse(
    // ...
    val isArchived: Boolean,
    val archivedAt: LocalDateTime?,
    val archivedBy: Long?,
    val archivedIp: String?,
    // ...
)
```

`PublicVideoResponse`에는 **`isArchived` 한 개만** 추가. `archivedBy/Ip` 같은 운영 정보는 공개 응답에 포함하지 않는다.

### 4. Repository 검색 조건 (`VideoRepositoryImpl.searchForAdmin`)

기존 `where(...)` 블록에 한 줄 추가 (`results` 조회 / `total` count 양쪽 모두):

```kotlin
request.isArchived?.let { video.isArchived.eq(it) },
```

`VideoRepositoryCustom`에는 변경 없음 — 시그니처가 `AdminVideoSearchRequest`를 받으므로 DTO 확장만으로 전달됨.

### 5. Service (`VideoService.setArchivedForAdmin`)

```kotlin
@Transactional
fun setArchivedForAdmin(id: Long, isArchived: Boolean): AdminVideoResponse {
    val video = videoRepository.findById(id).orElseThrow {
        BusinessException("동영상을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
    }

    val userId = authenticationService.getCurrentUserId()
    val clientIp = RequestUtils.getClientIp()

    if (isArchived) {
        video.isArchived = true
        video.archivedAt = LocalDateTime.now()
        video.archivedBy = userId
        video.archivedIp = clientIp
    } else {
        video.isArchived = false
        video.archivedAt = null
        video.archivedBy = null
        video.archivedIp = null
    }

    videoArchiveLogRepository.save(
        VideoArchiveLog(
            videoId = video.id!!,
            isArchived = isArchived,
            actionBy = userId,
            actionIp = clientIp
        )
    )

    val channelProfileUrl = urlBuilder.channelProfileUrl(video.channel?.uuid!!)
    return AdminVideoResponse.from(
        video = video,
        channelProfileUrl = channelProfileUrl,
        thumbnailUrl = urlBuilder.videoThumbnailUrl(video.uuid),
        playlistUrl = urlBuilder.videoPlaylistUrl(video.uuid)
    )
}
```

생성자에 `authenticationService: AuthenticationService`, `videoArchiveLogRepository: VideoArchiveLogRepository` 주입 추가.

**멱등 처리 없음**: 호출당 1건 로그를 항상 남긴다. 같은 상태로의 토글(이미 archived 상태에서 archive 호출)도 로그 1건 + 필드 갱신이 발생. 단일 PATCH 요청이라 클라이언트가 토글 직전 상태를 보고 명시적으로 의도한 액션이므로 자연스러움.

### 6. Controller (`AdminVideoController`)

```kotlin
@Operation(summary = "동영상 소장 여부 설정")
@PatchMapping("/{id}/archive")
fun setArchivedAdminVideo(
    @PathVariable id: Long,
    @RequestBody request: AdminVideoArchiveRequest
): AdminVideoResponse {
    return videoService.setArchivedForAdmin(id, request.isArchived)
}
```

기존 `PUT /admin/videos/{id}` 와 분리. 토글은 권한·감사 로깅의 의미가 다르고 목록에서 단독 호출이 잦으므로 별도 엔드포인트가 적절.

### 7. docs/schema.sql 동기화

`videos` 테이블에 컬럼 4개 + 인덱스 2개 추가, `video_archive_logs` 테이블 정의 추가. (실 DB 반영은 ddl-auto가 담당, 문서는 참조용)

---

### 8. Frontend: 관리자 동영상 목록 (`app/admin/videos/page.tsx`)

**검색 필터**: "소장만 보기" Checkbox를 검색 영역 끝에 추가.

```tsx
const [searchArchived, setSearchArchived] = useQueryState("archived", parseAsBoolean.withDefault(false));
const [draftSearchArchived, setDraftSearchArchived] = useState(searchArchived);

// ...
<label className="flex items-center gap-2">
    <Checkbox checked={draftSearchArchived} onCheckedChange={(v) => setDraftSearchArchived(!!v)} />
    <span className="text-sm">소장만 보기</span>
</label>
```

`searchParams.request.isArchived`는 `searchArchived ? true : undefined` (false면 전체).

**테이블 컬럼**: "공개 범위" 옆에 "소장" 컬럼 추가, shadcn `Switch`로 토글.

```tsx
<TableCell className="border-r text-center">
    <Switch
        checked={video.isArchived}
        onCheckedChange={(checked) => handleToggleArchive(video, checked)}
        disabled={archiveMutation.isPending}
    />
</TableCell>
```

```tsx
const archiveMutation = useSetArchivedAdminVideo();

const handleToggleArchive = async (video: AdminVideoResponse, checked: boolean) => {
    try {
        await archiveMutation.mutateAsync({ id: video.id, data: { isArchived: checked } });
        toast.success(`"${video.title}" 동영상이 ${checked ? '소장' : '소장 해제'}되었습니다.`);
        queryClient.invalidateQueries({ queryKey: ["/admin/videos"] });
    } catch {
        toast.error("처리에 실패했습니다.");
    }
};
```

**제목 셀**: `<Link>` 직후 `video.isArchived && <Bookmark size={14} className="..." fill="currentColor" />` 조건부 렌더.

`colSpan={9}` 값들은 컬럼 추가에 맞춰 +1 갱신.

### 9. Frontend: 동영상 상세 메뉴 (`components/video-info.tsx`)

기존 `adminMenu` DropdownMenu의 "수정" 위(또는 "삭제" 위)에 항목 추가:

```tsx
<DropdownMenuItem
    disabled={!adminVideo || archiveMutation.isPending}
    onClick={() => handleToggleArchive(!adminVideo!.isArchived)}
>
    {adminVideo?.isArchived ? <BookmarkX className="h-4 w-4" /> : <Bookmark className="h-4 w-4" />}
    {adminVideo?.isArchived ? '소장 해제' : '소장'}
</DropdownMenuItem>
```

핸들러:

```tsx
const archiveMutation = useSetArchivedAdminVideo();

const handleToggleArchive = async (next: boolean) => {
    if (!adminVideo) return;
    try {
        await archiveMutation.mutateAsync({ id: adminVideo.id, data: { isArchived: next } });
        toast.success(next ? '동영상이 소장되었습니다.' : '소장이 해제되었습니다.');
        await queryClient.invalidateQueries({ queryKey: ['/admin/videos'] });
        router.refresh();
    } catch {
        toast.error('처리에 실패했습니다.');
    }
};
```

### 10. Frontend: 제목 옆 소장 아이콘 (모든 사용자 노출)

**상세 페이지** (`video-info.tsx`): `<h1>{video.title}</h1>` 옆에:

```tsx
<h1 className="min-w-0 flex-1 text-xl font-bold flex items-center gap-2">
    {video.title}
    {video.isArchived && (
        <Bookmark size={18} className="text-primary flex-shrink-0" fill="currentColor" />
    )}
</h1>
```

**카드** (`video-card.tsx`): `VideoCardProps`에 `isArchived?: boolean` 추가, `<h3>` 옆에 동일 아이콘.

```tsx
<h3 className="text-md font-medium line-clamp-2 inline-flex items-start gap-1">
    {title}
    {isArchived && (
        <Bookmark size={14} className="mt-1 text-primary flex-shrink-0" fill="currentColor" />
    )}
</h3>
```

**카드 호출부 일괄 수정**: `grep -rn "VideoCard" frontend/src/app frontend/src/components`로 사용처 찾아 모두 `isArchived={video.isArchived}` (혹은 그에 준하는 필드명) 전달. `PublicVideoResponse`에 `isArchived`가 노출되므로 그대로 매핑됨.

**관리자 테이블 제목 셀**도 동일 아이콘 표시 (위 8번에 포함).

### 11. 권한

- 토글 Switch / 드롭다운 항목 자체는 ADMIN만 노출 (관리자 페이지는 라우트 가드, 상세는 기존 `isAdmin` prop으로 분기)
- 아이콘 표시는 권한 검사 없이 무조건 렌더
- 백엔드 `PATCH /admin/videos/{id}/archive`는 `SecurityConfig`의 `/admin/**` 가드로 자동 ADMIN 보호 (기존 동영상 수정/삭제와 동일)

---

## 재사용하는 기존 함수/패턴

- **`AuthenticationService.getCurrentUserId()`** + **`RequestUtils.getClientIp()`**: `AuthService.kt:56,127`에서 동일하게 created_by/created_ip 채우는 방식 그대로 차용.
- **`@SQLRestriction("is_active = true")` + soft delete 패턴** (`Video.kt:23`): 변경하지 않음 — 소장 여부는 별개 축.
- **shadcn `Switch`/`Checkbox`/`DropdownMenuItem`**: 기존 컴포넌트 그대로.
- **`useQueryClient.invalidateQueries({ queryKey: ["/admin/videos"] })` + toast**: 기존 video 수정/삭제 흐름과 동일.
- **lucide `Bookmark`, `BookmarkX`**: 신규 import, 다른 의존성 추가 없음.

신규 외부 라이브러리는 추가하지 않는다.

---

## 검증 방법

### 1. Backend

```bash
cd backend
./gradlew build
./gradlew bootRun
```

- 부팅 후 DB에서 `DESC videos;`로 컬럼 4개(`is_archived`, `archived_at`, `archived_by`, `archived_ip`) 생성 확인
- `DESC video_archive_logs;`로 신규 테이블 생성 확인
- Swagger UI(`/swagger-ui`)에서 `PATCH /admin/videos/{id}/archive` 노출 확인
- 임의 영상에 대해 토글 호출 → `videos.is_archived` / `videos.archived_*` 갱신 + `video_archive_logs`에 ARCHIVE 1건 INSERT 확인
- 해제 호출 → `archived_*` 모두 NULL + UNARCHIVE 로그 1건 추가

### 2. Frontend

```bash
cd frontend
pnpm orval        # 백엔드 변경 후 클라이언트 재생성
pnpm run dev
```

- `/admin/videos` 접속
  - "소장만 보기" 체크 → 목록 필터링
  - 임의 행의 "소장" Switch 토글 → toast + 즉시 반영 + 제목 옆 북마크 아이콘 표시/제거
  - URL에 `?archived=true` 쿼리 유지 확인 (nuqs)
- 동영상 상세 (ADMIN 로그인) → `...` 메뉴에서 "소장"/"소장 해제" 항목 노출, 클릭 시 toast + 페이지 새로고침 시 상태 반영, 제목 옆 아이콘 표시
- 비로그인/일반 사용자로 동영상 상세/홈 카드 접속 → 토글/메뉴는 안 보이고 **소장된 영상 제목 옆 아이콘은 표시됨**

### 3. 타입/빌드

```bash
cd frontend
pnpm run build
```

타입 에러 0개.

---

## 잠재 이슈 및 대응

- **`PublicVideoResponse`에 `isArchived` 추가로 인한 기존 카드 호출부 누락**: TypeScript 컴파일러가 prop 타입 변경을 잡아주지 않을 수 있음 (optional이므로). 빌드 후 브라우저로 모든 카드 사용처(홈, 채널 페이지, 검색 결과 등)를 한 번씩 확인.
- **멱등 호출로 로그가 부풀어 오를 가능성**: 1차 구현은 호출당 1건 누적. 실 사용에서 의도치 않은 중복 토글이 잦으면 멱등 처리(현재 상태와 동일하면 no-op) 추가 검토.
- **`(is_archived, created_at)` 복합 인덱스**: 자동 삭제 단계의 핵심 쿼리를 미리 가속화. 영상 수가 적은 현 시점에는 불필요해 보일 수 있으나, 인덱스는 1번 단계에서 같이 추가하는 게 추후 마이그레이션 비용을 줄임.
- **JPA `ddl-auto`로 인덱스가 의도대로 생성되지 않을 가능성**: ddl-auto가 `update` 모드면 인덱스 추가는 반영되지만, 환경에 따라 누락될 수 있음. 부팅 후 `SHOW INDEX FROM videos;`로 확인.
- **소장 해제 시 정보 손실**: 마지막으로 누가 archive했는지가 영상 필드에서는 사라지지만, `video_archive_logs`에서 조회 가능. 운영 페이지에서 로그 조회 UI는 본 작업 범위 밖.
