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
  private int totalUsers;
  private int successCount;
  private int failureCount;
  private String ageGroup;
  private int kakaoTalkCount;
  private int smsCount;
}