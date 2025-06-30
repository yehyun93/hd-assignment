package com.hyundai.autoever.security.assignment.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendRequestDto {
  private String ageGroup; // 연령대 (예: "20대", "30대")
  private String customMessage; // 추가 메시지 (선택사항)
}