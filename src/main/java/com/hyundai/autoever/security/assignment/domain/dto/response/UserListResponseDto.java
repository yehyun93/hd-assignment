package com.hyundai.autoever.security.assignment.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponseDto {
  private Long id;
  private String userId;
  private String name;
  private String phoneNumber;
  private String residentNumber;
  private String address;
}