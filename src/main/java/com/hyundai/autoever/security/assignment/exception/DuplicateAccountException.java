package com.hyundai.autoever.security.assignment.exception;

public class DuplicateAccountException extends RuntimeException {
  public DuplicateAccountException(String message) {
    super(message);
  }
}