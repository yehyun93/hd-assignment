package com.hyundai.autoever.security.assignment.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  @NotBlank(message = "계정은 필수입니다")
  private String userId;

  @Column(nullable = false)
  @NotBlank(message = "암호는 필수입니다")
  private String password;

  @Column(nullable = false)
  @NotBlank(message = "성명은 필수입니다")
  private String name;

  @Column(unique = true, nullable = false, length = 13)
  @Pattern(regexp = "\\d{13}", message = "주민등록번호는 13자리 숫자여야 합니다")
  private String residentNumber;

  @Column(nullable = false, length = 11)
  @Pattern(regexp = "\\d{11}", message = "핸드폰번호는 11자리 숫자여야 합니다")
  private String phoneNumber;

  @Column(nullable = false)
  @NotBlank(message = "주소는 필수입니다")
  private String address;
}