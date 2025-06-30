# 보안서비스개발팀 Backend 개발자 과제

## 📋 과제 개요
Spring Boot 기반의 회원 관리 시스템 및 3천만 고객 대상 메시지 발송 시스템 구현 과제입니다.

## 🚀 실행 방법

### 방법 1: Docker Compose 사용 (권장)
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

### 방법 2: 로컬 개별 실행
```bash
# 1. Redis 실행
docker run -d -p 6379:6379 redis:latest

# 2. WireMock 서버들 실행
# KakaoTalk Mock (터미널 1)
docker run -d -p 8081:8080 -v $(pwd)/mock-configs/kakao:/home/wiremock wiremock/wiremock:2.35.0 --global-response-templating --verbose

# SMS Mock (터미널 2)
docker run -d -p 8082:8080 -v $(pwd)/mock-configs/sms:/home/wiremock wiremock/wiremock:2.35.0 --global-response-templating --verbose

# 3. 메인 애플리케이션 실행 (터미널 3)
./gradlew bootRun
```

### 방법 3: 자동화 스크립트로 실행
```bash
./start-system.sh
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

### 4. 🎯 연령대별 대용량 메시지 발송 (핵심 기능)
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

## 🛠️ 표준 응답 구조

모든 API는 아래와 같은 표준 구조로 응답합니다.
```json
{
  "success": true,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": { ... },
  "timestamp": "2024-06-01T12:34:56Z",
  "code": "S001"
}
```

### 주요 응답 코드
- S001: 요청 성공
- S002: 회원가입 성공
- S003: 로그인 성공
- S008: 메시지 발송 성공
- S009: 대용량 메시지 발송 성공
- C400: 잘못된 요청/입력값 오류
- C401: 인증 실패
- C403: 권한 없음
- C404: 사용자 없음
- C409: 중복 계정/주민번호
- S500: 서버 오류

### 메시지 발송 결과 예시
```json
{
  "message": "30대 대상 메시지 발송 완료",
  "totalUsers": 1000,
  "successCount": 950,
  "failureCount": 50,
  "ageGroup": "30대",
  "kakaoTalkCount": 900,
  "smsCount": 50,
  "kakaoTalkRate": 0.9,
  "smsRate": 0.05
}
```

## 🔥 3천만 고객 메시지 발송 시스템

### ⚡ 핵심 특징
- **카카오톡 우선**: 1분당 100회 제한, 실패 시 SMS 자동 전환
- **SMS Fallback**: 1분당 500회 제한
- **Redis Rate Limiting**: 실시간 호출량 제어
- **비동기 처리**: WebFlux 기반 논블로킹 처리
- **연령대별 발송**: 주민등록번호 기반 연령대 계산

### 📊 메시지 발송 시나리오 테스트

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

### 📈 성능 모니터링
```bash
# 실시간 로그 모니터링
docker-compose logs -f security-app | grep "메시지 발송"

# Rate Limit 상황 확인
docker-compose logs -f security-app | grep -E "Rate|속도 제한"

# WireMock 서버 응답 확인
docker-compose logs -f kakao-mock sms-mock
```

## 🧪 장애 시뮬레이션

### WireMock 설정 기반 시나리오
- **카카오톡**: 인증 실패, 잘못된 요청 시나리오
- **SMS**: 인증 실패 시나리오
- **네트워크 지연**: WireMock 설정으로 시뮬레이션 가능

### 테스트용 사용자 생성
```bash
./scripts/create-test-users.sh
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
│   ├── util/                # Rate Limiter, Age Calculator
│   └── ...
├── mock-configs/            # WireMock 설정 파일들
│   ├── kakao/               # 카카오톡 API 모킹 설정
│   └── sms/                 # SMS API 모킹 설정
├── src/main/resources/
│   ├── application.yml      # 기본 설정
│   └── application-docker.yml # Docker 환경 설정
├── docker-compose.yml       # 전체 인프라 구성
├── start-system.sh          # 자동화 실행 스크립트
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

## 🔍 트러블슈팅

### 일반적인 문제
- **Redis 연결 실패**: `docker ps`로 Redis 컨테이너 확인
- **Mock 서버 응답 없음**: 포트 충돌 확인 (8081, 8082)
- **메모리 부족**: Docker Desktop 메모리 할당량 증가
- **권한 오류**: `./gradlew`에 실행 권한 부여

### 로그 레벨 조정
```yaml
# application.yml
logging:
  level:
    com.hyundai.autoever.security.assignment: DEBUG  # 상세 로그
```