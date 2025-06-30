package com.hyundai.autoever.security.assignment.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimiter {

  private final RedisTemplate<String, String> redisTemplate;

  /**
   * 카카오톡 메시지 발송 속도 제한 (1분당 100회)
   */
  public boolean isKakaoTalkAllowed() {
    try {
      String key = "kakaotalk:rate:limit:" + getCurrentMinute();
      return checkRateLimit(key, 100, Duration.ofMinutes(1));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * SMS 메시지 발송 속도 제한 (1분당 500회)
   */
  public boolean isSmsAllowed() {
    try {
      String key = "sms:rate:limit:" + getCurrentMinute();
      return checkRateLimit(key, 500, Duration.ofMinutes(1));
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * 속도 제한을 확인하고 증가시킵니다.
   */
  private boolean checkRateLimit(String key, int limit, Duration window) {
    try {
      String currentCount = redisTemplate.opsForValue().get(key);
      if (currentCount == null) {
        redisTemplate.opsForValue().set(key, "1", window.toSeconds(), TimeUnit.SECONDS);
        return true;
      }
      int count = Integer.parseInt(currentCount);
      if (count < limit) {
        redisTemplate.opsForValue().increment(key);
        return true;
      }
      return false;
    } catch (Exception e) {
      return true;
    }
  }

  /**
   * 현재 분을 반환합니다 (YYYYMMDDHHMM 형식)
   */
  private String getCurrentMinute() {
    return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
  }
}