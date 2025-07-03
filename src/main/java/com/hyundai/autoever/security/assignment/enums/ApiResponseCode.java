package com.hyundai.autoever.security.assignment.enums;

public enum ApiResponseCode {
  // 회원가입 관련
  REGISTER_SUCCESS("S002", "회원가입이 완료되었습니다."),
  LOGIN_SUCCESS("S003", "로그인이 완료되었습니다."),
  LOGOUT_SUCCESS("S004", "로그아웃이 완료되었습니다."),

  // 메시지 발송 관련
  MESSAGE_SEND_SUCCESS("S008", "메시지 발송이 완료되었습니다."),
  BULK_MESSAGE_SUCCESS("S009", "대용량 메시지 발송이 완료되었습니다."),

  // 클라이언트 오류 (4xx)
  BAD_REQUEST("C400", "잘못된 요청입니다."),
  VALIDATION_FAILED("C400", "입력값 검증에 실패했습니다."),
  UNAUTHORIZED("C401", "인증이 필요합니다."),
  INVALID_CREDENTIALS("C401", "아이디 또는 비밀번호가 올바르지 않습니다."),
  FORBIDDEN("C403", "해당 리소스에 접근할 권한이 없습니다."),
  USER_NOT_FOUND("C404", "사용자를 찾을 수 없습니다."),
  DUPLICATE_ACCOUNT("C409", "이미 존재하는 계정입니다."),
  DUPLICATE_RESIDENT_NUMBER("C409", "이미 존재하는 주민등록번호입니다."),
  INVALID_PASSWORD("C422", "비밀번호가 일치하지 않습니다."),

  // 서버 오류 (5xx)
  INTERNAL_ERROR("S500", "서버 내부 오류가 발생했습니다."),
  DATABASE_ERROR("S501", "데이터베이스 오류가 발생했습니다."),
  EXTERNAL_API_ERROR("S502", "외부 API 호출 중 오류가 발생했습니다."),
  MESSAGE_SEND_ERROR("S503", "메시지 발송 중 오류가 발생했습니다.");

  private final String code;
  private final String message;

  ApiResponseCode(String code, String message) {
    this.code = code;
    this.message = message;
  }

  // 편의 메서드들
  public boolean isSuccess() {
    return this.code.startsWith("S") && !this.code.startsWith("S5");
  }

  public boolean isClientError() {
    return this.code.startsWith("C");
  }

  public boolean isServerError() {
    return this.code.startsWith("S5");
  }

  public String getCode() {
    return this.code;
  }

  public String getMessage() {
    return this.message;
  }
}
