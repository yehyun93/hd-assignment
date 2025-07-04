package com.hyundai.autoever.security.assignment.exception;

import com.hyundai.autoever.security.assignment.common.enums.ApiResponseCode;

public class DuplicateAccountException extends RuntimeException {
  public DuplicateAccountException() {
    super(ApiResponseCode.DUPLICATE_ACCOUNT.getMessage());
  }
}