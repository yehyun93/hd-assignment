package com.hyundai.autoever.security.assignment.config;

import com.hyundai.autoever.security.assignment.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Slf4j
@Converter
@Component
@RequiredArgsConstructor
public class EncryptedStringConverter implements AttributeConverter<String, String> {

  private final CryptoUtil cryptoUtil;

  @Override
  public String convertToDatabaseColumn(String attribute) {
    if (attribute == null) {
      return null;
    }

    try {
      String encrypted = cryptoUtil.encrypt(attribute);
      log.debug("주민번호 암호화 완료");
      return encrypted;
    } catch (Exception e) {
      log.error("DB 저장 시 암호화 실패", e);
      throw new RuntimeException("개인정보 암호화 처리 중 오류가 발생했습니다");
    }
  }

  @Override
  public String convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }

    try {
      String decrypted = cryptoUtil.decrypt(dbData);
      log.debug("주민번호 복호화 완료");
      return decrypted;
    } catch (Exception e) {
      log.error("DB 조회 시 복호화 실패", e);
      throw new RuntimeException("개인정보 복호화 처리 중 오류가 발생했습니다");
    }
  }
}