package com.hyundai.autoever.security.assignment.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponseDto {
  private Long id;
  private String userId;
  private String name;
  private String residentNumber;
  private String phoneNumber;
  private String address; // 행정구역 단위만 제공 (예: "서울특별시", "경기도")
}