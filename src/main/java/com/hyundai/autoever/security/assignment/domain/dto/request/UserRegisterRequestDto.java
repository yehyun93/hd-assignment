package com.hyundai.autoever.security.assignment.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequestDto {
  @NotBlank(message = "계정은 필수입니다")
  private String userId;

  @NotBlank(message = "암호는 필수입니다")
  @Size(min = 8, message = "암호는 8자 이상이어야 합니다")
  private String password;

  @NotBlank(message = "성명은 필수입니다")
  private String name;

  @Pattern(regexp = "\\d{13}", message = "주민등록번호는 13자리 숫자여야 합니다")
  private String residentNumber;

  @Pattern(regexp = "\\d{11}", message = "핸드폰번호는 11자리 숫자여야 합니다")
  private String phoneNumber;

  @NotBlank(message = "주소는 필수입니다")
  private String address;
}