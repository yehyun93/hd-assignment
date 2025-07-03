package com.hyundai.autoever.security.assignment.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.hyundai.autoever.security.assignment.enums.AgeGroup;
import jakarta.validation.constraints.NotNull;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendRequestDto {
  @NotNull
  private AgeGroup ageGroup;
  private String customMessage;
}