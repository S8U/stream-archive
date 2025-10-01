# Coding Conventions

## 기본 원칙

- **Kotlin**: [Google Kotlin Style Guide](https://developer.android.com/kotlin/style-guide) 준수
- **Java**: [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) 준수
- **로그**: 모든 로그 메시지는 영어로 작성

## Backend (Kotlin + Spring Boot)

### 로깅

- 모든 로그 메시지는 **영어**로 작성한다
- 로그 레벨을 적절히 사용한다
  - `ERROR`: 시스템 오류, 즉시 조치 필요
  - `WARN`: 잠재적 문제, 모니터링 필요
  - `INFO`: 주요 이벤트, 상태 변경
  - `DEBUG`: 디버깅 정보
  - `TRACE`: 상세한 실행 흐름

**Good:**
```kotlin
logger.info("Initializing global settings")
logger.error("Failed to start recording: streamId={}", streamId)
logger.debug("Polling interval: {} seconds", interval)
```

**Bad:**
```kotlin
logger.info("전역 설정 초기화 시작")
logger.error("녹화 시작 실패: streamId={}", streamId)
```

### Transaction 관리

- `@Transactional` 어노테이션은 **클래스 레벨이 아닌 메소드 레벨**에 선언한다
- 조회 메소드는 `@Transactional(readOnly = true)` 사용
- 변경 메소드는 `@Transactional` 사용

**Good:**
```kotlin
@Service
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun getUser(id: Long): User? {
        return userRepository.findById(id).orElse(null)
    }

    @Transactional
    fun createUser(user: User): User {
        return userRepository.save(user)
    }
}
```

**Bad:**
```kotlin
@Service
@Transactional(readOnly = true)  // 클래스 레벨에 선언하지 않음
class UserService(
    private val userRepository: UserRepository
) {
    fun getUser(id: Long): User? {
        return userRepository.findById(id).orElse(null)
    }

    @Transactional
    fun createUser(user: User): User {
        return userRepository.save(user)
    }
}
```

### Entity 설계

1. **Column name을 명시하지 않음** - JPA가 자동으로 snake_case로 변환
2. **FK(Foreign Key) 관계를 사용하지 않음** - ID 필드만 사용
3. **모든 컬럼에 `@Comment` 추가** - 데이터베이스 스키마 문서화

**Good:**
```kotlin
@Entity
@Table(name = "users")
@Comment("사용자")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("사용자 ID")
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    @Comment("사용자명")
    val username: String
)
```

**Bad:**
```kotlin
@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "username", nullable = false, length = 100)  // name 명시하지 않음
    val username: String  // @Comment 누락
)
```