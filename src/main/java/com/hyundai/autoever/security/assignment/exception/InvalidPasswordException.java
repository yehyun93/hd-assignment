package com.hyundai.autoever.security.assignment.exception;

import com.hyundai.autoever.security.assignment.enums.ApiResponseCode;

public class InvalidPasswordException extends RuntimeException {
  public InvalidPasswordException() {
    super(ApiResponseCode.INVALID_PASSWORD.getMessage());
  }
}