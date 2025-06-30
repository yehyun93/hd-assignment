package com.hyundai.autoever.security.assignment.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
  private Long id;
  private String userId;
  private String name;
  private String residentNumber;
  private String phoneNumber;
  private String address;
}