package com.hyundai.autoever.security.assignment.controller;

import com.hyundai.autoever.security.assignment.domain.dto.request.UserRegisterRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.UserRegisterResponseDto;
import com.hyundai.autoever.security.assignment.common.dto.ApiResponse;
import com.hyundai.autoever.security.assignment.domain.dto.request.UserLoginRequestDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.UserLoginResponseDto;
import com.hyundai.autoever.security.assignment.domain.dto.response.UserDetailResponseDto;
import com.hyundai.autoever.security.assignment.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
  private final UserService userService;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<UserRegisterResponseDto>> register(
      @Valid @RequestBody UserRegisterRequestDto requestDto) {
    return ResponseEntity.ok(ApiResponse.success(userService.register(requestDto)));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<UserLoginResponseDto>> login(@Valid @RequestBody UserLoginRequestDto requestDto) {
    return ResponseEntity.ok(ApiResponse.success(userService.login(requestDto)));
  }

  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<UserDetailResponseDto>> getMyInfo(Authentication authentication) {
    String userId = authentication.getName();
    return ResponseEntity.ok(ApiResponse.success(userService.getMyInfo(userId)));
  }
}