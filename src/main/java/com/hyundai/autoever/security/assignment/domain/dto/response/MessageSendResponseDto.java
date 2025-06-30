package com.hyundai.autoever.security.assignment.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendResponseDto {
  private String message;
  private int totalUsers;
  private int successCount;
  private int failureCount;
  private String ageGroup;
  
  // 📊 채널별 발송 건수 추가
  private int kakaoTalkCount;
  private int smsCount;
  
  // 📈 발송 비율 정보 (선택적)
  private Double kakaoTalkRate;
  private Double smsRate;
}