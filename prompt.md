# 📑 개발 자동화 및 과제 프롬프트

- Cursor IDE를 기반으로 소스코드의 80%를 자동화
- Claude Desktop + mcp를 활용하여 소스코드 재검증 및 문서화 작업 진행

---

- 주어진 요구사항 분석하고 필요한 구현 내용들 정의해서 Requirement.md 파일로 만들어줘
- 사용자/관리자 기능 기준으로 controller를 나눌꺼야
  그리고 Requirement.md 파일 기반으로 1단계 회원가입 api 구현해줘 restful하게 설계해서 구현해
- 중복에 대한 예외는 어떻게 처리하는게 Best 일까?
- 1단계 요구사항 모두 반영되었는지 확인하고 덜 구현된 부분이 있으면 수정해줘
- 구현 완료되었으면 성공/실패 Case에 대한 테스트 코드 작성해줘
- 작성한 테스트 코드가 제대로 작동하는지 테스트 해봐
- 완료 되었으면 Requirement.md에 진행사항 업데이트 해줘
<br><br>

- 이제 2단계 시스템 관리자 API 구현해봐
- 요구사항을 기반으로 구현한다고 했을때 관리자를 DB에까지 저장하는 로직이 필요하다고 생각해?
- http://localhost:8080/admin/users 호출하면
{
    "code": "INTERNAL_ERROR",
    "message": "서버 내부 오류가 발생했습니다."
}
  이런 에러가 return 되는데 무슨 이유일까?
  오류 분석하고 수정해봐
- 성공/실패 Case에 대한 테스트 코드 작성해봐
- 테스트 코드가 정상 작동하는지 확인해봐
- 2단계까지의 요구사항 구현여부 확인하고 완료된 항목은 체크표시 해줘
<br><br>

- 3단계 사용자 로그인 api 구현해줘
- 4단계 사용자 상세정보조회 api 구현해줘
- 모든 행정구역 정보를 문자열로 비교하는 것보다 정규식으로 추출해내도록 수정해봐
- 순환참조 오류가 발생되는데 원인 파악하고 수정해봐
- 성공/실패 Case에 대한 테스트 코드 작성하고 테스트 코드 정상 작동여부 확인해봐
- 요구사항 구현여부 확인하고 Requirement.md 파일 업데이트 해줘
- 여기까지 구현한 내용에 대한 API 명세서 작성해줘
<br><br>

- 5단계 요구사항은 어떤 방향으로 진행하는게 좋을까?
- 위 제안을 기반으로 3천만 고객 대상 메세지 발송 프로세스 구현해봐
- 메세지 발송 프로세스를 상세히 알려줘
- 메세지 발송 기능도 관리자 기능인데 controller를 분리하는게 맞을까?
- 현재 구현된 소스코드가 3천만명을 대상으로 메세지를 보내는 기능을 수행하기에 적합한지 검증해줘
- 메세지 발송대상 사용자를 읽어올때 페이징 처리하는 로직 추기해줘 oom이 발생되지 않게
<br><br>

- docker로 서비스를 분리할껀데 어떻게 분리하는게 좋을까?
- 실무가 아니라 과제로 제출할 용도라고 했을때 어느정도로 구성하면 좋을지 추천해봐
- 말한 구조를 기반으로 docker 환경 세팅을 위한 설정파일들 작성해봐
- kakao, sms 서비스 테스트를 위한 mock 서버는 어떻게 세팅하는게 제일 좋을까?
<br><br>

- 이 프로젝트를 분석했을때,
  3천만건의 메세지를 처리하는 구조로써 적합한지 확인해줘
  하지만 실제 환경이 아닌 과제 제출용이기에 실제 환경처럼 완전히 구현하긴 힘들다는걸 기반으로 분석해줘
- 전체 패키지에서 불필요한 로그나 주석 정리해줘
- security-app 서비스에서 오류가 발생되는데 추적해봐
- 메세지 발송 테스트를 위해 대량의 사용자를 추가하는 스크립트 만들어줘
- 메세지 발송 api 호출 시 401 에러가 발생돼
  아마도 서비스 간의 인증 문제인 것 같은데, 요구사항을 기반으로 다시 설명해보자면 /api/message/send 호출 시 -> admin:1212 계정 인증을 통해 권한 확인-> http://localhost:8081/kakaotalk-messages 호출 시 해당 서비스 내부에서는 autoever:1234 계정의 인증 정보를 확인해야함
  현재는 과제를 위한 구현이라 같은 프로젝트에 구현했지만, 실제로는 각각의 서비스라고 생각하고 각각의 인증을 진행해야해
  어떻게 구현하면 좋을까?
- wiremock을 활용하는 방식으로 환경을 다시 세팅해봐
- 불필요한 소스코드들 제거해줘
- 서버 다시 올리고 테스트 케이스들 검증해봐
<br><br>

- docker 서비스가 다 정상적으로 올라왔는데도 8080 포트로 요청하면 실패해 원인 파악해봐
- 서비스 다시 올리고 사용자 생성한 다음 메세지 발송해봐
- 로그를 확인해보면
2025-06-30 00:28:04 [http-nio-8080-exec-2] ERROR a.test.security.util.RateLimiter - Redis 작업 중 오류 발생: Unable to connect to Redis
  이런 에러가 찍혀있어 Redis가 제대로 연결되지 않는 것 같아
- 메세지 발송에는 성공하는데 successCount가 0이야 원인 파악해봐
- 카톡 발송에 실패해서 실패하는 case들을 발생시켜야해 10% 정도의 확률로 실패하는 로직 구현해줘
- 도커 올려서 메세지 발송 다시 테스트해봐
<br><br>

- ApiResponse.java로 api 응답 형식을 통일화하고 싶어 관련된 소스코드들 리팩토링 해줘
- code 필드는 enum으로 관리할꺼야 응답 코드값과 메세지를 포함시킬꺼야 수정해봐
- TestCase들도 변경된 응답 형식에 맞게 수정하고 테스트 진행해줘
- 예외처리가 일관성 없이 처리되고 있는 부분이 많은 것 같아
  GlobalExceptionHandler를 활용하도록 try-catch로 처리되고 있는 예외들을 통일화해줘
- 전체 패키지 내에서 사용되지 않거나 불필요한 파일들 제거해줘
- 불필요한 로그 및 주석들도 제거해줘
- 소스코드 분석 후 Readme.md 파일 업데이트 해줘
<br><br>

### Claude Desktop 관련 추가 프롬프트

- 첨부한 과제를 수행중이야
  과제 내용과 구현된 서비스를 구현했을때 누락된 부분이나 리팩토링이 필요한 부분이 있을까?
  그리고 요구사항이 명확히 충족 되었는지도 확인해줘
  특히 마지막 요구사항인 3천만 고객 대상 메시징 기능을 중점으로 확인해줘
- security 폴더 하위의 패키지 구조 확인 후 더 나은 패키지 구조가 있다면 추천해줘
  지금으로도 괜찮다면 수정하지 않을꺼야
- 일반적인 대고객 서비스에서 3천만 고객 대상 메세지를 보낼때 얼마정도의 소요시간을 기준으로 구현해?
- 이걸 과제 수준에서 구현한다고 하면 지금 정도의 Level로도 적합할까?
- RateLimit 테스트를 위한 사용자를 생성하는 스크립트를 짜줘
- security 폴더 하위에 구현한 소스코드가 존재하고,
  그 코드를 기반으로 Readme.md 파일을 작성했어
  확인해보고 구성이 어떤지, 변경해야할 부분이나 개선해야 할 부분
  혹은 추가해야할 부분이 있는지 파악해줘
- 현재 패키지 구조가 적절한지 분석하고, 개선이 필요한 파일이나 폴더가 있다면 추천해줘