package com.hyundai.autoever.security.assignment.exception;

import com.hyundai.autoever.security.assignment.common.enums.ApiResponseCode;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException() {
    super(ApiResponseCode.USER_NOT_FOUND.getMessage());
  }
}