package com.hyundai.autoever.security.assignment.exception;

import com.hyundai.autoever.security.assignment.common.enums.ApiResponseCode;

public class DuplicateResidentNumberException extends RuntimeException {
  public DuplicateResidentNumberException() {
    super(ApiResponseCode.DUPLICATE_RESIDENT_NUMBER.getMessage());
  }
}