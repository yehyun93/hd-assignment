package com.hyundai.autoever.security.assignment.domain.dto.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
  private boolean success;
  private String message;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T data;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String timestamp;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String code;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Object errors;

  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .data(data)
        .build();
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .message(message)
        .data(data)
        .build();
  }

  public static <T> ApiResponse<T> success(ApiResponseCode responseCode, T data) {
    return ApiResponse.<T>builder()
        .success(true)
        .data(data)
        .build();
  }

  public static <T> ApiResponse<T> success(ApiResponseCode responseCode) {
    return ApiResponse.<T>builder()
        .success(true)
        .message(responseCode.getMessage())
        .build();
  }

  public static <T> ApiResponse<T> error(ApiResponseCode responseCode) {
    return ApiResponse.<T>builder()
        .success(false)
        .message(responseCode.getMessage())
        .timestamp(Instant.now().toString())
        .code(responseCode.getCode())
        .build();
  }

  public static <T> ApiResponse<T> error(ApiResponseCode responseCode, String customMessage) {
    return ApiResponse.<T>builder()
        .success(false)
        .message(customMessage)
        .timestamp(Instant.now().toString())
        .code(responseCode.getCode())
        .build();
  }

  public static <T> ApiResponse<T> validationError(Object errors) {
    return ApiResponse.<T>builder()
        .success(false)
        .message("입력값 검증에 실패했습니다.")
        .timestamp(Instant.now().toString())
        .code("C400")
        .errors(errors)
        .build();
  }

  public static <T> ApiResponse<T> error(ApiResponseCode responseCode, Object errors) {
    return ApiResponse.<T>builder()
        .success(false)
        .message(responseCode.getMessage())
        .timestamp(Instant.now().toString())
        .code(responseCode.getCode())
        .errors(errors)
        .build();
  }
}