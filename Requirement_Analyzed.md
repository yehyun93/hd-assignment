# 보안서비스개발팀 서버(Backend) 개발자 과제전형 - 요구사항 분석

## 📋 과제 개요
- **과제명**: Spring Boot 기반 보안 서비스 개발
- **개발 언어**: Java 또는 Kotlin
- **프레임워크**: Spring Boot
- **제출 방식**: GitHub 저장소 (public)

## 🛠️ 개발 환경 요구사항

### 필수 구성요소
- [x] Spring Boot 프레임워크 기반 서버 구현
- [x] README.md 작성 (면접관이 로컬에서 실행 가능하도록)
- [ ] PROMPT.md 작성 (AI assistance 활용 프롬프트 기록)
- [x] 저장소 설정 (DB, Redis 등 자유롭게 사용, 실행 가이드 포함)

### AI 활용 가이드
- [ ] AI assistance 최대한 적극 활용
- [ ] 모든 프롬프트를 PROMPT.md에 기록

## 🔐 API 요구사항 (순차 구현 필수)

### 1단계: 회원가입 API
**엔드포인트**: 사용자 회원가입 API

**입력 필드**:
- [x] 계정 (유일값)
- [x] 암호
- [x] 성명
- [x] 주민등록번호 (유일값, 11자리 형식)
- [x] 핸드폰번호 (11자리 형식)
- [x] 주소

**제약사항**:
- [x] 계정과 주민등록번호는 시스템 내 유일값
- [x] 실제 본인인증 불필요 (과제용 데이터)
- [x] 서버는 사용자 요청을 신뢰하여 처리

### 2단계: 시스템 관리자 API
**인증 방식**: Basic Auth
- 사용자명: `admin`
- 암호: `1212`

**기능**:
- [x] 회원 조회 API (pagination 기반)
- [x] 회원 수정 API (암호, 주소만 수정 가능)
- [x] 회원 삭제 API

**수정 제약사항**:
- [x] 암호와 주소만 수정 가능
- [x] 개별 수정 또는 동시 수정 모두 지원

### 3단계: 사용자 로그인 API
**기능**:
- [x] 회원가입한 사용자 로그인
- [x] 로그인 성공 시 세션/토큰 발급

### 4단계: 사용자 상세정보 조회 API
**기능**:
- [x] 로그인한 사용자의 본인 상세정보 조회
- [x] 주소 정보는 최대 행정구역 단위만 제공
  - 예: "서울특별시", "경기도", "강원특별자치도"

### 5단계: 대용량 서비스 대응 (3천만 사용자)
**기능**: 연령대별 카카오톡 메시지 발송 API

**메시지 형식**:
- [x] 첫 줄: "{회원 성명}님, 안녕하세요. 현대 오토에버입니다."

**발송 정책**:
- [x] 카카오톡 메시지 실패 시 SMS 문자메시지로 대체
- [x] 카카오톡: 1분당 100회 제한
- [x] SMS: 분당 500회 제한

## 🔌 외부 API 연동

### 카카오톡 메시지 API
```
POST http://localhost:8081/kakaotalk-messages
Headers:
- Authorization: Basic auth (autoever:1234)
- Content-Type: application/json
Body: {"phone": "xxx-xxxx-xxxx", "message": "blabla"}
Response: 200/400/401/500 (바디 없음)
```

### SMS 메시지 API
```
POST http://localhost:8082/sms?phone={phone}
Headers:
- Authorization: Basic auth (autoever:5678)
- Content-Type: application/x-www-form-urlencoded
Body: {"message": "blabla"}
Response: 200/400/401/500
Response Body: {"result": "OK"}
```

## 📊 기술적 고려사항

### 성능 요구사항
- [x] 3천만 사용자 처리 가능한 구조
- [x] 메시지 발송 제한 처리
- [x] Pagination 기반 조회 최적화

### 보안 요구사항
- [x] Basic Auth 구현
- [x] 사용자 데이터 보호
- [x] API 인증/인가 처리

### 확장성 요구사항
- [x] 대용량 사용자 처리
- [x] 외부 API 연동
- [x] 메시지 발송 시스템

## 📝 제출 요구사항

### 필수 파일
- [x] README.md (서버 실행 가이드)
- [ ] PROMPT.md (AI 활용 기록)
- [x] 소스코드
- [x] 데이터베이스/저장소 실행 가이드

### 구현 순서
1. [x] 회원가입 API 완료 후 다음 단계 진행
2. [x] 각 단계별 완전한 구현 필요
3. [x] 순차적 개발 진행

## 🎯 평가 기준
- [x] 요구사항 완전 구현 (1-5단계)
- [x] 코드 품질
- [x] 문서화 완성도
- [ ] AI 활용도
- [x] 실행 가능성 