package com.hyundai.autoever.security.assignment.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimiter 테스트")
class RateLimiterTest {

  @Mock
  private RedisTemplate<String, String> redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @InjectMocks
  private RateLimiter rateLimiter;

  @BeforeEach
  void setUp() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
  }

  @Test
  @DisplayName("카카오톡 속도 제한 - 첫 번째 요청 허용")
  void isKakaoTalkAllowed_FirstRequest_Allowed() {
    // Given
    when(valueOperations.get(anyString())).thenReturn(null);
    doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

    // When
    boolean result = rateLimiter.isKakaoTalkAllowed();

    // Then
    assertTrue(result);
    verify(valueOperations).get(anyString());
    verify(valueOperations).set(anyString(), eq("1"), eq(60L), eq(TimeUnit.SECONDS));
  }

  @Test
  @DisplayName("카카오톡 속도 제한 - 제한 내 요청 허용")
  void isKakaoTalkAllowed_WithinLimit_Allowed() {
    // Given
    when(valueOperations.get(anyString())).thenReturn("50");
    when(valueOperations.increment(anyString())).thenReturn(51L);

    // When
    boolean result = rateLimiter.isKakaoTalkAllowed();

    // Then
    assertTrue(result);
    verify(valueOperations).get(anyString());
    verify(valueOperations).increment(anyString());
  }

  @Test
  @DisplayName("카카오톡 속도 제한 - 제한 초과 요청 거부")
  void isKakaoTalkAllowed_ExceedLimit_Denied() {
    // Given
    when(valueOperations.get(anyString())).thenReturn("100");

    // When
    boolean result = rateLimiter.isKakaoTalkAllowed();

    // Then
    assertFalse(result);
    verify(valueOperations).get(anyString());
    verify(valueOperations, never()).increment(anyString());
  }

  @Test
  @DisplayName("SMS 속도 제한 - 첫 번째 요청 허용")
  void isSmsAllowed_FirstRequest_Allowed() {
    // Given
    when(valueOperations.get(anyString())).thenReturn(null);
    doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

    // When
    boolean result = rateLimiter.isSmsAllowed();

    // Then
    assertTrue(result);
    verify(valueOperations).get(anyString());
    verify(valueOperations).set(anyString(), eq("1"), eq(60L), eq(TimeUnit.SECONDS));
  }

  @Test
  @DisplayName("SMS 속도 제한 - 제한 내 요청 허용")
  void isSmsAllowed_WithinLimit_Allowed() {
    // Given
    when(valueOperations.get(anyString())).thenReturn("400");
    when(valueOperations.increment(anyString())).thenReturn(401L);

    // When
    boolean result = rateLimiter.isSmsAllowed();

    // Then
    assertTrue(result);
    verify(valueOperations).get(anyString());
    verify(valueOperations).increment(anyString());
  }

  @Test
  @DisplayName("SMS 속도 제한 - 제한 초과 요청 거부")
  void isSmsAllowed_ExceedLimit_Denied() {
    // Given
    when(valueOperations.get(anyString())).thenReturn("500");

    // When
    boolean result = rateLimiter.isSmsAllowed();

    // Then
    assertFalse(result);
    verify(valueOperations).get(anyString());
    verify(valueOperations, never()).increment(anyString());
  }

  @Test
  @DisplayName("Redis 예외 발생 시 카카오톡 허용")
  void isKakaoTalkAllowed_RedisException_Allowed() {
    // Given
    when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis 연결 오류"));

    // When
    boolean result = rateLimiter.isKakaoTalkAllowed();

    // Then
    assertTrue(result);
  }

  @Test
  @DisplayName("Redis 예외 발생 시 SMS 허용")
  void isSmsAllowed_RedisException_Allowed() {
    // Given
    when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis 연결 오류"));

    // When
    boolean result = rateLimiter.isSmsAllowed();

    // Then
    assertTrue(result);
  }

  @Test
  @DisplayName("카카오톡 속도 제한 - 경계값 테스트")
  void isKakaoTalkAllowed_BoundaryValue() {
    // Given
    when(valueOperations.get(anyString())).thenReturn("99");
    when(valueOperations.increment(anyString())).thenReturn(100L);

    // When
    boolean result = rateLimiter.isKakaoTalkAllowed();

    // Then
    assertTrue(result);
    verify(valueOperations).increment(anyString());
  }

  @Test
  @DisplayName("SMS 속도 제한 - 경계값 테스트")
  void isSmsAllowed_BoundaryValue() {
    // Given
    when(valueOperations.get(anyString())).thenReturn("499");
    when(valueOperations.increment(anyString())).thenReturn(500L);

    // When
    boolean result = rateLimiter.isSmsAllowed();

    // Then
    assertTrue(result);
    verify(valueOperations).increment(anyString());
  }

  @Test
  @DisplayName("카카오톡 속도 제한 - 정수 파싱 오류 시 허용")
  void isKakaoTalkAllowed_ParseError_Allowed() {
    // Given
    when(valueOperations.get(anyString())).thenReturn("invalid_number");

    // When
    boolean result = rateLimiter.isKakaoTalkAllowed();

    // Then
    assertTrue(result);
  }

  @Test
  @DisplayName("SMS 속도 제한 - 정수 파싱 오류 시 허용")
  void isSmsAllowed_ParseError_Allowed() {
    // Given
    when(valueOperations.get(anyString())).thenReturn("invalid_number");

    // When
    boolean result = rateLimiter.isSmsAllowed();

    // Then
    assertTrue(result);
  }

  @Test
  @DisplayName("카카오톡 속도 제한 - Redis 설정 실패 시 허용")
  void isKakaoTalkAllowed_SetFailure_Allowed() {
    // Given
    when(valueOperations.get(anyString())).thenReturn(null);
    doThrow(new RuntimeException("Redis 설정 오류")).when(valueOperations).set(anyString(), anyString(), anyLong(),
        any(TimeUnit.class));

    // When
    boolean result = rateLimiter.isKakaoTalkAllowed();

    // Then
    assertTrue(result);
  }

  @Test
  @DisplayName("SMS 속도 제한 - Redis 설정 실패 시 허용")
  void isSmsAllowed_SetFailure_Allowed() {
    // Given
    when(valueOperations.get(anyString())).thenReturn(null);
    doThrow(new RuntimeException("Redis 설정 오류")).when(valueOperations).set(anyString(), anyString(), anyLong(),
        any(TimeUnit.class));

    // When
    boolean result = rateLimiter.isSmsAllowed();

    // Then
    assertTrue(result);
  }

  @Test
  @DisplayName("카카오톡 속도 제한 - Redis 증가 실패 시 허용")
  void isKakaoTalkAllowed_IncrementFailure_Allowed() {
    // Given
    when(valueOperations.get(anyString())).thenReturn("50");
    when(valueOperations.increment(anyString())).thenThrow(new RuntimeException("Redis 증가 오류"));

    // When
    boolean result = rateLimiter.isKakaoTalkAllowed();

    // Then
    assertTrue(result);
  }

  @Test
  @DisplayName("SMS 속도 제한 - Redis 증가 실패 시 허용")
  void isSmsAllowed_IncrementFailure_Allowed() {
    // Given
    when(valueOperations.get(anyString())).thenReturn("400");
    when(valueOperations.increment(anyString())).thenThrow(new RuntimeException("Redis 증가 오류"));

    // When
    boolean result = rateLimiter.isSmsAllowed();

    // Then
    assertTrue(result);
  }

  @Test
  @DisplayName("연속 호출 시 카카오톡 속도 제한 동작")
  void isKakaoTalkAllowed_ConsecutiveCalls() {
    // Given
    when(valueOperations.get(anyString())).thenReturn("1", "2", "3", "4", "5");
    when(valueOperations.increment(anyString())).thenReturn(2L, 3L, 4L, 5L, 6L);

    // When & Then
    for (int i = 0; i < 5; i++) {
      boolean result = rateLimiter.isKakaoTalkAllowed();
      assertTrue(result);
    }

    verify(valueOperations, times(5)).get(anyString());
    verify(valueOperations, times(5)).increment(anyString());
  }

  @Test
  @DisplayName("연속 호출 시 SMS 속도 제한 동작")
  void isSmsAllowed_ConsecutiveCalls() {
    // Given
    when(valueOperations.get(anyString())).thenReturn("1", "2", "3", "4", "5");
    when(valueOperations.increment(anyString())).thenReturn(2L, 3L, 4L, 5L, 6L);

    // When & Then
    for (int i = 0; i < 5; i++) {
      boolean result = rateLimiter.isSmsAllowed();
      assertTrue(result);
    }

    verify(valueOperations, times(5)).get(anyString());
    verify(valueOperations, times(5)).increment(anyString());
  }
}