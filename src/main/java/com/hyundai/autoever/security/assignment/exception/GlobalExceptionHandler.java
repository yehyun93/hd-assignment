package com.hyundai.autoever.security.assignment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hyundai.autoever.security.assignment.domain.dto.response.ApiResponse;
import com.hyundai.autoever.security.assignment.domain.dto.response.ApiResponseCode;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.HashMap;
import org.springframework.validation.FieldError;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    log.warn("입력 검증 실패: {}", errors);
    return ResponseEntity.badRequest()
        .body(ApiResponse.validationError(errors));
  }

  @ExceptionHandler(DuplicateAccountException.class)
  public ResponseEntity<ApiResponse<Void>> handleDuplicateAccount(DuplicateAccountException ex) {
    log.warn("계정 중복: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiResponse.error(ApiResponseCode.DUPLICATE_ACCOUNT));
  }

  @ExceptionHandler(DuplicateResidentNumberException.class)
  public ResponseEntity<ApiResponse<Void>> handleDuplicateResidentNumber(DuplicateResidentNumberException ex) {
    log.warn("주민등록번호 중복: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiResponse.error(ApiResponseCode.DUPLICATE_RESIDENT_NUMBER));
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
    log.warn("사용자 없음: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(ApiResponseCode.USER_NOT_FOUND));
  }

  @ExceptionHandler(InvalidPasswordException.class)
  public ResponseEntity<ApiResponse<Void>> handleInvalidPassword(InvalidPasswordException ex) {
    log.warn("비밀번호 불일치: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(ApiResponse.error(ApiResponseCode.INVALID_PASSWORD));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
    log.warn("접근 권한 없음: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ApiResponse.error(ApiResponseCode.FORBIDDEN));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
    log.warn("인증 실패: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error(ApiResponseCode.UNAUTHORIZED));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
    log.error("예상치 못한 오류: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(ApiResponseCode.INTERNAL_ERROR));
  }
}