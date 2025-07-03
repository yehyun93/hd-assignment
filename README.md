# 보안서비스개발팀 Backend 개발자 과제

## 📋 과제 개요
Spring Boot 기반의 회원 관리 시스템 및 3천만 고객 대상 메시지 발송 시스템 구현 과제입니다.

## 🚀 실행 방법

### Docker Compose 사용
```bash
# 1. 프로젝트 클론
git clone [repository-url]
cd security

# 2. 전체 서비스 실행 (메인 앱 + Redis + WireMock 서버들)
docker-compose up -d

# 3. 서비스 확인
- 메인 애플리케이션: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console
- 카카오톡 WireMock API: http://localhost:8081
- SMS WireMock API: http://localhost:8082
- Redis: localhost:6379
```

### 테스트용 사용자 생성
```bash
./scripts/create-test-users.sh
```

## 📝 API 테스트 가이드

### 1. 회원가입
```bash
curl -X POST http://localhost:8080/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "testuser",
    "password": "password123",
    "name": "홍길동",
    "residentNumber": "9001011234567",
    "phoneNumber": "01012345678",
    "address": "서울특별시 강남구 테헤란로 123"
  }'
```

### 2. 로그인
```bash
curl -X POST http://localhost:8080/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "testuser",
    "password": "password123"
  }'
```

### 3. 관리자 API - 회원 조회/상세/수정/삭제 (Basic Auth)
```bash
# 전체 회원 조회 (페이징)
curl -X GET http://localhost:8080/admin/users \
  -u admin:1212

# 단일 회원 조회
curl -X GET http://localhost:8080/admin/users/{userId} \
  -u admin:1212

# 회원 정보 수정 (비밀번호/주소)
curl -X PUT http://localhost:8080/admin/users/{userId} \
  -u admin:1212 \
  -H "Content-Type: application/json" \
  -d '{"password": "newpass", "address": "서울특별시"}'

# 회원 삭제
curl -X DELETE http://localhost:8080/admin/users/{userId} \
  -u admin:1212
```

### 4. 연령대별 대용량 메시지 발송 (핵심 기능)
```bash
curl -X POST http://localhost:8080/admin/messages/send-by-age-group \
  -u admin:1212 \
  -H "Content-Type: application/json" \
  -d '{
    "ageGroup": "30대",
    "customMessage": "30대 고객 대상 테스트 진행 중입니다."
  }'
```

### 5. 메시지 발송 진행률 조회
```bash
curl -X GET http://localhost:8080/admin/messages/progress \
  -u admin:1212
```

## 🛠️ 표준 응답 구조 (실제 구현 기준)

모든 API 응답은 아래와 같은 구조로 반환됩니다.

```json
{
  "success": true,                // 요청 성공 여부 (true/false)
  "message": "상태 메시지",        // 처리 결과 메시지
  "data": { ... },                // 실제 데이터 (없을 경우 null)
  "timestamp": "2024-06-01T12:34:56Z", // 오류 발생 시 타임스탬프
  "code": "S001",                 // 응답 코드 (성공/실패/오류 등)
  "errors": { ... }               // 입력값 검증 오류 등 상세 에러 (선택)
}
```

- `success`: 성공 여부 (`true`/`false`)
- `message`: 처리 결과 메시지 (상황별로 다름)
- `data`: 실제 응답 데이터 (예: 회원 정보, 토큰, 메시지 발송 결과 등)
- `timestamp`: 오류 발생 시 현재 시각(ISO8601)
- `code`: 응답 코드(아래 표 참고)
- `errors`: 입력값 검증 실패 등 상세 오류 정보(필요 시)

### 응답 예시

#### 회원가입 성공
```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "userId": "testuser",
    "name": "홍길동"
  },
  "code": "S002"
}
```

#### 입력값 검증 실패
```json
{
  "success": false,
  "message": "입력값 검증에 실패했습니다.",
  "code": "C400",
  "timestamp": "2024-06-01T12:34:56Z",
  "errors": {
    "password": "8자 이상 입력해야 합니다."
  }
}
```

#### 인증 실패
```json
{
  "success": false,
  "message": "인증이 필요합니다.",
  "code": "C401",
  "timestamp": "2024-06-01T12:34:56Z"
}
```

#### 대용량 메시지 발송 결과
```json
{
  "success": true,
  "message": "대용량 메시지 발송이 완료되었습니다.",
  "data": {
    "totalUsers": 1000,
    "successCount": 950,
    "failureCount": 50,
    "ageGroup": "30대",
    "kakaoTalkCount": 900,
    "smsCount": 50
  },
  "code": "S009"
}
```

---

### 주요 응답 코드 (실제 Enum 기준)

| 코드   | 의미                        |
|--------|-----------------------------|
| S002   | 회원가입 성공               |
| S003   | 로그인 성공                 |
| S004   | 로그아웃 성공               |
| S008   | 메시지 발송 성공            |
| S009   | 대용량 메시지 발송 성공     |
| C400   | 잘못된 요청/입력값 오류     |
| C401   | 인증 실패                   |
| C403   | 권한 없음                   |
| C404   | 사용자 없음                 |
| C409   | 중복 계정/주민번호          |
| C422   | 비밀번호 불일치             |
| S500   | 서버 내부 오류              |
| S501   | 데이터베이스 오류           |
| S502   | 외부 API 오류               |
| S503   | 메시지 발송 중 오류         |

---

### 예외 및 오류 응답 처리 방식
- 모든 예외는 GlobalExceptionHandler에서 일괄 처리
- 입력값 검증 실패: 400, 상세 오류 필드 포함
- 인증/권한 오류: 401/403
- 중복/존재하지 않는 계정: 409/404
- 서버/외부 API 오류: 5xx
- 모든 오류 응답에 `success: false`, `message`, `code`, `timestamp` 포함

## 🔥 3천만 고객 메시지 발송 시스템

### ⚡ 핵심 특징
- **카카오톡 우선**: 1분당 100회 제한, 실패 시 SMS 자동 전환
- **SMS Fallback**: 1분당 500회 제한
- **Redis Rate Limiting**: 실시간 호출량 제어
- **비동기 처리**: WebFlux 기반 논블로킹 처리
- **연령대별 발송**: 주민등록번호 기반 연령대 계산
- **실시간 성공/실패 카운팅**
- **WireMock 기반 외부 API 모킹**

### 메시지 발송 시나리오 테스트

#### Rate Limiting 테스트
```bash
for i in {1..150}; do
  curl -X POST http://localhost:8080/admin/messages/send-by-age-group \
    -u admin:1212 \
    -H "Content-Type: application/json" \
    -d '{"ageGroup": "20대", "customMessage": "테스트 메시지 '$i'"}' &
done
wait
```

#### WireMock API 직접 테스트
```bash
# 카카오톡 WireMock API 테스트
curl -X POST http://localhost:8081/kakaotalk-messages \
  -H "Authorization: Basic $(echo -n 'autoever:1234' | base64)" \
  -H "Content-Type: application/json" \
  -d '{"phone": "010-1234-5678", "message": "테스트 메시지"}'

# SMS WireMock API 테스트
curl -X POST "http://localhost:8082/sms?phone=010-1234-5678" \
  -H "Authorization: Basic $(echo -n 'autoever:5678' | base64)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "message=테스트 SMS 메시지"
```

## 📊 데이터베이스 접속
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `1234`

## 🛠 기술 스택
- **Framework**: Spring Boot 3.5.3
- **Security**: Spring Security + JWT + Basic Auth
- **Database**: H2 Database + Spring Data JPA
- **Cache**: Redis (Rate Limiting)
- **Async**: WebFlux (비동기 메시지 발송)
- **Infrastructure**: Docker & Docker Compose
- **Mock**: WireMock (외부 API 모킹)
- **Monitoring**: Spring Actuator

## 📁 프로젝트 구조
```
security/
├── src/main/java/com.hyundai.autoever.security.assignment/
│   ├── config/              # 보안 설정, Redis 설정
│   ├── controller/          # 사용자/관리자 API
│   ├── service/             # 비즈니스 로직
│   │   ├── BulkMessageService # 대용량 메시지 발송
│   │   ├── KakaoTalkService # 카카오톡 API 호출
│   │   └── SmsService       # SMS API 호출
│   ├── util/                # Rate Limiter, Age Calculator, JWT 등
│   └── ...
├── mock-configs/            # WireMock 설정 파일들
│   ├── kakao/               # 카카오톡 API 모킹 설정
│   └── sms/                 # SMS API 모킹 설정
├── src/main/resources/
│   ├── application.yml      # 기본 설정
│   └── application-docker.yml # Docker 환경 설정
├── docker-compose.yml       # 전체 인프라 구성
├── scripts/                 # 테스트 유저 생성 스크립트 등
└── README.md
```

## ✨ 핵심 구현 사항

### 1. 회원 관리 시스템
- ✅ 회원가입/로그인 (계정, 주민등록번호 중복 검증)
- ✅ JWT 기반 사용자 인증
- ✅ Basic Auth 기반 관리자 인증 (admin:1212)
- ✅ Pagination 기반 회원 조회
- ✅ 회원 정보 수정/삭제

### 2. 대량 메시지 발송 시스템
- ✅ 연령대별 메시지 발송 (주민등록번호 기반)
- ✅ 카카오톡 → SMS Fallback 로직
- ✅ Redis 기반 Rate Limiting (카카오톡: 100/분, SMS: 500/분)
- ✅ 비동기 병렬 처리 (WebFlux)
- ✅ 실시간 성공/실패 카운팅

### 3. WireMock 기반 Mock 서버 (과제 요구사항)
- ✅ 카카오톡 WireMock API (8081 포트)
- ✅ SMS WireMock API (8082 포트)
- ✅ 설정 파일 기반 간단한 관리

## 🎯 메시지 템플릿
모든 메시지는 다음 형식으로 발송됩니다:
```
{회원 성명}님, 안녕하세요. 현대 오토에버입니다.
{사용자 정의 메시지}
```