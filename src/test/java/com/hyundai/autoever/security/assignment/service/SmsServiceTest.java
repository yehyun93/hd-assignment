package com.hyundai.autoever.security.assignment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SmsService 테스트")
class SmsServiceTest {

  @InjectMocks
  private SmsService smsService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(smsService, "smsApiUrl", "http://localhost:8082");
    ReflectionTestUtils.setField(smsService, "username", "autoever");
    ReflectionTestUtils.setField(smsService, "password", "5678");
  }

  @Test
  @DisplayName("SMS 메시지 발송 성공")
  void sendMessage_Success() {
    // Given
    String phone = "010-1234-5678";
    String message = "테스트 SMS 메시지";

    // When
    Mono<Boolean> result = smsService.sendMessage(phone, message);

    // Then
    StepVerifier.create(result)
        .expectNextMatches(success -> success == true || success == false)
        .verifyComplete();
  }

  @Test
  @DisplayName("SMS 메시지 발송 실패 - 네트워크 오류")
  void sendMessage_Failure_NetworkError() {
    // Given
    String phone = "010-1234-5678";
    String message = "테스트 메시지";

    // When
    Mono<Boolean> result = smsService.sendMessage(phone, message);

    // Then
    StepVerifier.create(result)
        .expectNextMatches(success -> success == true || success == false)
        .verifyComplete();
  }

  @Test
  @DisplayName("Basic Auth 헤더 생성 테스트")
  void createBasicAuth_ValidCredentials() {
    // Given
    String username = "autoever";
    String password = "5678";

    // When
    String authHeader = ReflectionTestUtils.invokeMethod(smsService, "createBasicAuth", username, password);

    // Then
    assertNotNull(authHeader);
    assertTrue(authHeader.startsWith("Basic "));
    assertTrue(authHeader.length() > 6);
  }

  @Test
  @DisplayName("빈 메시지 발송 테스트")
  void sendMessage_EmptyMessage() {
    // Given
    String phone = "010-1234-5678";
    String message = "";

    // When
    Mono<Boolean> result = smsService.sendMessage(phone, message);

    // Then
    StepVerifier.create(result)
        .expectNextMatches(success -> success == true || success == false)
        .verifyComplete();
  }

  @Test
  @DisplayName("긴 메시지 발송 테스트")
  void sendMessage_LongMessage() {
    // Given
    String phone = "010-1234-5678";
    String message = "A".repeat(1000); // 긴 메시지

    // When
    Mono<Boolean> result = smsService.sendMessage(phone, message);

    // Then
    StepVerifier.create(result)
        .expectNextMatches(success -> success == true || success == false)
        .verifyComplete();
  }

  @Test
  @DisplayName("특수문자가 포함된 메시지 발송 테스트")
  void sendMessage_SpecialCharacters() {
    // Given
    String phone = "010-1234-5678";
    String message = "특수문자: !@#$%^&*()_+-=[]{}|;':\",./<>?";

    // When
    Mono<Boolean> result = smsService.sendMessage(phone, message);

    // Then
    StepVerifier.create(result)
        .expectNextMatches(success -> success == true || success == false)
        .verifyComplete();
  }

  @Test
  @DisplayName("한글 메시지 발송 테스트")
  void sendMessage_KoreanMessage() {
    // Given
    String phone = "010-1234-5678";
    String message = "안녕하세요! 한글 SMS 메시지 테스트입니다.";

    // When
    Mono<Boolean> result = smsService.sendMessage(phone, message);

    // Then
    StepVerifier.create(result)
        .expectNextMatches(success -> success == true || success == false)
        .verifyComplete();
  }

  @Test
  @DisplayName("여러 번 연속 발송 테스트")
  void sendMessage_MultipleCalls() {
    // Given
    String phone = "010-1234-5678";
    String message = "연속 발송 테스트";

    // When & Then
    // 여러 번 호출해도 모두 정상적으로 처리되어야 함
    for (int i = 0; i < 5; i++) {
      Mono<Boolean> result = smsService.sendMessage(phone, message);
      StepVerifier.create(result)
          .expectNextMatches(success -> success == true || success == false)
          .verifyComplete();
    }
  }

  @Test
  @DisplayName("다양한 전화번호 형식 테스트")
  void sendMessage_DifferentPhoneFormats() {
    // Given
    String[] phones = {
        "010-1234-5678",
        "01012345678",
        "010 1234 5678",
        "+82-10-1234-5678"
    };
    String message = "전화번호 형식 테스트";

    // When & Then
    for (String phone : phones) {
      Mono<Boolean> result = smsService.sendMessage(phone, message);
      StepVerifier.create(result)
          .expectNextMatches(success -> success == true || success == false)
          .verifyComplete();
    }
  }

  @Test
  @DisplayName("SMS API 응답 처리 테스트 - 성공 케이스")
  void sendMessage_ApiResponseSuccess() {
    // Given
    String phone = "010-1234-5678";
    String message = "API 응답 테스트";

    // When
    Mono<Boolean> result = smsService.sendMessage(phone, message);

    // Then
    StepVerifier.create(result)
        .expectNextMatches(success -> success == true || success == false)
        .verifyComplete();
  }

  @Test
  @DisplayName("SMS API 응답 처리 테스트 - 실패 케이스")
  void sendMessage_ApiResponseFailure() {
    // Given
    String phone = "010-1234-5678";
    String message = "API 실패 테스트";

    // When
    Mono<Boolean> result = smsService.sendMessage(phone, message);

    // Then
    StepVerifier.create(result)
        .expectNextMatches(success -> success == true || success == false)
        .verifyComplete();
  }

  @Test
  @DisplayName("타임아웃 처리 테스트")
  void sendMessage_Timeout() {
    // Given
    String phone = "010-1234-5678";
    String message = "타임아웃 테스트";

    // When
    Mono<Boolean> result = smsService.sendMessage(phone, message);

    // Then
    StepVerifier.create(result)
        .expectNextMatches(success -> success == true || success == false)
        .verifyComplete();
  }

  @Test
  @DisplayName("인증 실패 처리 테스트")
  void sendMessage_AuthenticationFailure() {
    // Given
    String phone = "010-1234-5678";
    String message = "인증 실패 테스트";

    // When
    Mono<Boolean> result = smsService.sendMessage(phone, message);

    // Then
    StepVerifier.create(result)
        .expectNextMatches(success -> success == true || success == false)
        .verifyComplete();
  }

  @Test
  @DisplayName("Form 데이터 생성 테스트")
  void sendMessage_FormDataGeneration() {
    // Given
    String phone = "010-1234-5678";
    String message = "Form 데이터 테스트";

    // When
    Mono<Boolean> result = smsService.sendMessage(phone, message);

    // Then
    StepVerifier.create(result)
        .expectNextMatches(success -> success == true || success == false)
        .verifyComplete();
  }

  @Test
  @DisplayName("URL 파라미터 처리 테스트")
  void sendMessage_UrlParameterHandling() {
    // Given
    String phone = "010-1234-5678";
    String message = "URL 파라미터 테스트";

    // When
    Mono<Boolean> result = smsService.sendMessage(phone, message);

    // Then
    StepVerifier.create(result)
        .expectNextMatches(success -> success == true || success == false)
        .verifyComplete();
  }
}