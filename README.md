# 보안서비스개발팀 Backend 개발자 과제

## 📋 과제 개요
Spring Boot 기반의 회원 관리 시스템 및 3천만 고객 대상 메시지 발송 시스템 구현 과제입니다.

## 🛠️ 기술 스택

- **Language**: Java 17
- **Framework**: Spring Boot 3.5.3
- **Security**: Spring Security + JWT + Basic Auth
- **Database**: H2 Database + Spring Data JPA
- **Cache**: Redis (Rate Limiting)
- **Async**: WebFlux (비동기 메시지 발송)
- **Infrastructure**: Docker & Docker Compose
- **Mock**: WireMock (외부 API 모킹)
- **Test**: JUnit 5, MockMvc
- **Monitoring**: Spring Actuator

## 🚀 API 테스트 가이드

### 1. 프로젝트 클론 및 실행
```bash
# 프로젝트 클론
git clone https://github.com/yehyun93/hd-assignment.git
cd security

# 전체 서비스 실행 (한 번에 모든 서비스 시작)
docker-compose up -d

# 서비스 상태 확인
docker-compose ps
```

### 2. 서비스 접속 확인
| 서비스 | URL | 설명 |
|--------|-----|------|
| 메인 애플리케이션 | http://localhost:8080 | Spring Boot API 서버 |
| H2 Console | http://localhost:8080/h2-console | 데이터베이스 콘솔 |
| 카카오톡 Mock API | http://localhost:8081 | WireMock 카카오톡 서버 |
| SMS Mock API | http://localhost:8082 | WireMock SMS 서버 |
| Redis | localhost:6379 | Redis 캐시 서버 |

### 3. 테스트용 데이터 생성
```bash
# 다양한 연령대의 테스트 사용자 생성 (약 16명)
chmod +x ./scripts/create-test-users.sh
./scripts/create-test-users.sh

# 대량 메시지 테스트용 사용자 생성 (약 1000명)
chmod +x ./scripts/create-message-test-users.sh
./scripts/create-message-test-users.sh
```

## 📝 상세 API 가이드

### 👤 사용자 API

#### 회원가입
```bash
POST /users/register
Content-Type: application/json

{
  "userId": "string (4-20자)",
  "password": "string (8-50자)",
  "name": "string (2-10자)",
  "residentNumber": "string (13자리)",
  "phoneNumber": "string (11자리)",
  "address": "string (10-100자)"
}
```

#### 로그인
```bash
POST /users/login
Content-Type: application/json

{
  "userId": "string",
  "password": "string"
}

# 응답
{
  "success": true,
  "data": {
    "accessToken": "JWT_TOKEN",
    "userId": "testuser001"
  }
}
```

#### 본인 정보 조회
```bash
GET /users/me
Authorization: Bearer {JWT_ACCESS_TOKEN}

# 응답 (주소는 최상위 행정구역만 반환)
{
  "success": true,
  "data": {
    "userId": "testuser001",
    "name": "홍길동",
    "phoneNumber": "01012345678",
    "address": "서울특별시"
  }
}
```

### 🛡️ 관리자 API (Basic Auth: admin:1212)

#### 전체 회원 조회 (페이징)
```bash
GET /admin/users?page=0&size=20
Authorization: Basic admin:1212

# 응답
{
  "success": true,
  "data": {
    "content": [...],
    "pagination": {
      "currentPage": 0,
      "totalPages": 5,
      "totalElements": 100,
      "pageSize": 20,
      "hasNext": true,
      "hasPrevious": false
    }
  }
}
```

#### 단일 회원 조회
```bash
GET /admin/users/{userId}
Authorization: Basic admin:1212
```

#### 회원 정보 수정
```bash
PUT /admin/users/{userId}
Authorization: Basic admin:1212
Content-Type: application/json

{
  "password": "newpassword123",  # 선택사항
  "address": "부산광역시 해운대구"    # 선택사항
}
```

#### 회원 삭제
```bash
DELETE /admin/users/{userId}
Authorization: Basic admin:1212
```

#### 연령대별 대용량 메시지 발송
```bash
POST /admin/messages/send-by-age-group
Authorization: Basic admin:1212
Content-Type: application/json

{
  "ageGroup": "THIRTIES",
  "customMessage": "사용자 정의 메시지"
}

# 응답
{
  "success": true,
  "data": {
    "totalUsers": 1500,
    "successCount": 1450,
    "failureCount": 50,
    "ageGroup": "THIRTIES",
    "kakaoTalkCount": 1200,
    "smsCount": 250
  }
}
```

## 🎯 연령대 코드표

| 코드 | 나이 범위 | 한글명 | 주민번호 예시 |
|------|-----------|--------|---------------|
| UNDER_10 | 0~9세 | 10대 미만 | 15xxxxx, 16xxxxx |
| TEENS | 10~19세 | 10대 | 05xxxxx~14xxxxx |
| TWENTIES | 20~29세 | 20대 | 95xxxxx~04xxxxx |
| THIRTIES | 30~39세 | 30대 | 85xxxxx~94xxxxx |
| FORTIES | 40~49세 | 40대 | 75xxxxx~84xxxxx |
| FIFTIES | 50~59세 | 50대 | 65xxxxx~74xxxxx |
| SIXTIES | 60~69세 | 60대 | 55xxxxx~64xxxxx |
| SEVENTIES | 70~79세 | 70대 | 45xxxxx~54xxxxx |
| OVER_80 | 80세 이상 | 80대 이상 | ~44xxxxx |

## 🔥 3천만 고객 메시지 발송 시스템

### 핵심 특징
- **우선순위**: 카카오톡 → SMS Fallback
- **Rate Limiting**: 카카오톡 100/분, SMS 500/분
- **비동기 처리**: WebFlux 기반 논블로킹
- **배치 처리**: 100명 단위 페이징
- **동시성 제어**: 최대 5개 동시 요청
- **실시간 통계**: 성공/실패 카운팅

### 메시지 템플릿
```
{회원명}님, 안녕하세요. 현대 오토에버입니다.
{사용자 정의 메시지}
```

## 🧪 테스트 설정

### Fallback 로직 시연을 위한 인위적 실패
- 카카오톡 API 10% 확률로 실패 시뮬레이션
- SMS 전환 로직 확인 가능
- 실제 운영 시에는 `kakao.api.test.enabled: false`로 설정


### 성능 테스트
```bash
# Rate Limiting 테스트
for i in {1..10}; do
  curl -X POST http://localhost:8080/admin/messages/send-by-age-group \
    -u admin:1212 \
    -H "Content-Type: application/json" \
    -d '{"ageGroup": "TWENTIES", "customMessage": "테스트 '$i'"}' &
done
wait
```

## 🧾 API 응답 구조

### 성공 응답
```json
{
  "success": true,
  "data": {...},
}
```

### 에러 응답
```json
{
  "success": false,
  "message": "사용자를 찾을 수 없습니다.",
  "code": "C404",
  "timestamp": "2024-07-04T12:34:56Z"
}
```

### 에러 코드표
| 코드 | HTTP Status | 메시지 | 상황 |
|------|-------------|--------|------|
| C400 | 400 | 잘못된 요청입니다 | 입력값 오류 |
| C401 | 401 | 인증이 필요합니다 | 로그인 필요 |
| C403 | 403 | 권한이 없습니다 | 접근 권한 없음 |
| C404 | 404 | 사용자를 찾을 수 없습니다 | 존재하지 않는 리소스 |
| C409 | 409 | 이미 존재하는 계정입니다 | 중복 데이터 |
| S500 | 500 | 서버 내부 오류가 발생했습니다 | 서버 에러 |
| S503 | 503 | 메시지 발송 중 오류가 발생했습니다 | 메시지 발송 실패 |

## 🔒 보안 및 암호화

### 인증 방식
- **사용자**: JWT 기반 인증
- **관리자**: Basic Auth (admin:1212)
- **외부 API**: Basic Auth (각 서비스별 다른 계정)

### 데이터 암호화
- **비밀번호**: BCrypt 해시 (단방향)
- **주민등록번호**: AES-128 대칭키 (양방향)

## 🧪 개발/테스트 환경

### 테스트 실행
```bash
# 단위 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "AdminControllerTest"
```

### WireMock API 직접 테스트
```bash
# 카카오톡 API 테스트
curl -X POST http://localhost:8081/kakaotalk-messages \
  -H "Authorization: Basic $(echo -n 'autoever:1234' | base64)" \
  -H "Content-Type: application/json" \
  -d '{"phone": "010-1234-5678", "message": "테스트 메시지"}'

# SMS API 테스트  
curl -X POST "http://localhost:8082/sms?phone=010-1234-5678" \
  -H "Authorization: Basic $(echo -n 'autoever:5678' | base64)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "message=테스트 SMS 메시지"
```

## 📁 프로젝트 구조
```
security/
├── src/main/java/com.hyundai.autoever.security.assignment/
│   ├── config/              # 보안 설정, Redis 설정
│   ├── controller/          # REST API 컨트롤러
│   ├── service/             # 비즈니스 로직
│   ├── repository/          # 데이터 접근 계층
│   ├── domain/              # DTO, Entity
│   ├── util/                # 유틸리티 클래스
│   ├── exception/           # 예외 처리
│   └── enums/               # 열거형
├── mock-configs/            # WireMock 설정
│   ├── kakao/               # 카카오톡 API 모킹
│   └── sms/                 # SMS API 모킹
├── scripts/                 # 유틸리티 스크립트
├── docker-compose.yml       # 인프라 구성
└── README.md
```

## ✨ 구현 완료 기능

### 1. 회원 관리 시스템
- ✅ 회원가입/로그인 (중복 검증)
- ✅ JWT 기반 사용자 인증
- ✅ Basic Auth 기반 관리자 인증
- ✅ 페이징 기반 회원 조회
- ✅ 회원 정보 수정/삭제

### 2. 대량 메시지 발송 시스템  
- ✅ 연령대별 메시지 발송
- ✅ 카카오톡 → SMS Fallback 로직
- ✅ Redis 기반 Rate Limiting
- ✅ 비동기 병렬 처리 (WebFlux)
- ✅ 실시간 성공/실패 통계

### 3. 인프라 및 모킹
- ✅ Docker Compose 기반 서비스 구성
- ✅ WireMock 기반 외부 API 모킹
- ✅ Redis 캐시 연동
- ✅ 포괄적인 테스트 코드