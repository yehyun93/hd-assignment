package com.hyundai.autoever.security.assignment.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

import com.hyundai.autoever.security.assignment.enums.AgeGroup;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("AgeCalculator 테스트")
class AgeCalculatorTest {

  @InjectMocks
  private AgeCalculator ageCalculator;

  @BeforeEach
  void setUp() {
    // 테스트를 위한 현재 날짜 설정 (2024년 기준)
  }

  @Test
  @DisplayName("1900년대 출생자 연령 계산 - 남성")
  void calculateAge_1900s_Male() {
    log.info("현재 날짜: {}", LocalDate.now());

    String residentNumber = "9001011234567"; // 1990년 1월 1일 출생 남성
    int age = ageCalculator.calculateAge(residentNumber);
    assertEquals(35, age);
  }

  @Test
  @DisplayName("1900년대 출생자 연령 계산 - 여성")
  void calculateAge_1900s_Female() {
    // Given
    String residentNumber = "9001012234567"; // 1990년 1월 1일 출생 여성

    // When
    int age = ageCalculator.calculateAge(residentNumber);

    // Then
    assertEquals(35, age); // 2024년 기준 34세
  }

  @Test
  @DisplayName("2000년대 출생자 연령 계산 - 남성")
  void calculateAge_2000s_Male() {
    // Given
    String residentNumber = "0501013234567"; // 2005년 1월 1일 출생 남성

    // When
    int age = ageCalculator.calculateAge(residentNumber);

    // Then
    assertEquals(20, age); // 2024년 기준 19세
  }

  @Test
  @DisplayName("2000년대 출생자 연령 계산 - 여성")
  void calculateAge_2000s_Female() {
    // Given
    String residentNumber = "0501014234567"; // 2005년 1월 1일 출생 여성

    // When
    int age = ageCalculator.calculateAge(residentNumber);

    // Then
    assertEquals(20, age);
  }

  @Test
  @DisplayName("연령대 분류 테스트 - 10대 미만")
  void getAgeGroup_Under10() {
    // Given
    int age = 9;

    // When
    AgeGroup ageGroup = AgeGroup.fromAge(age);

    // Then
    assertEquals(AgeGroup.UNDER_10.getDisplayName(), ageGroup.getDisplayName());
  }

  @Test
  @DisplayName("연령대 분류 테스트 - 10대")
  void getAgeGroup_Teens() {
    // Given
    int age = 15;

    // When
    AgeGroup ageGroup = AgeGroup.fromAge(age);

    // Then
    assertEquals(AgeGroup.TEENS.getDisplayName(), ageGroup.getDisplayName());
  }

  @Test
  @DisplayName("연령대 분류 테스트 - 20대")
  void getAgeGroup_Twenties() {
    // Given
    int age = 25;

    // When
    AgeGroup ageGroup = AgeGroup.fromAge(age);

    // Then
    assertEquals(AgeGroup.TWENTIES.getDisplayName(), ageGroup.getDisplayName());
  }

  @Test
  @DisplayName("연령대 분류 테스트 - 30대")
  void getAgeGroup_Thirties() {
    // Given
    int age = 35;

    // When
    AgeGroup ageGroup = AgeGroup.fromAge(age);

    // Then
    assertEquals(AgeGroup.THIRTIES.getDisplayName(), ageGroup.getDisplayName());
  }

  @Test
  @DisplayName("연령대 분류 테스트 - 40대")
  void getAgeGroup_Forties() {
    // Given
    int age = 45;

    // When
    AgeGroup ageGroup = AgeGroup.fromAge(age);

    // Then
    assertEquals(AgeGroup.FORTIES.getDisplayName(), ageGroup.getDisplayName());
  }

  @Test
  @DisplayName("연령대 분류 테스트 - 50대")
  void getAgeGroup_Fifties() {
    // Given
    int age = 55;

    // When
    AgeGroup ageGroup = AgeGroup.fromAge(age);

    // Then
    assertEquals(AgeGroup.FIFTIES.getDisplayName(), ageGroup.getDisplayName());
  }

  @Test
  @DisplayName("연령대 분류 테스트 - 60대")
  void getAgeGroup_Sixties() {
    // Given
    int age = 65;

    // When
    AgeGroup ageGroup = AgeGroup.fromAge(age);

    // Then
    assertEquals(AgeGroup.SIXTIES.getDisplayName(), ageGroup.getDisplayName());
  }

  @Test
  @DisplayName("연령대 분류 테스트 - 70대")
  void getAgeGroup_Seventies() {
    // Given
    int age = 75;

    // When
    AgeGroup ageGroup = AgeGroup.fromAge(age);

    // Then
    assertEquals(AgeGroup.SEVENTIES.getDisplayName(), ageGroup.getDisplayName());
  }

  @Test
  @DisplayName("연령대 분류 테스트 - 80대 이상")
  void getAgeGroup_EightiesAndAbove() {
    // Given
    int age = 85;

    // When
    AgeGroup ageGroup = AgeGroup.fromAge(age);

    // Then
    assertEquals(AgeGroup.OVER_80.getDisplayName(), ageGroup.getDisplayName());
  }

  @Test
  @DisplayName("주민등록번호에서 연령대 직접 계산 - 20대")
  void getAgeGroupFromResidentNumber_Twenties() {
    // Given
    String residentNumber = "9501011234567"; // 1995년 1월 1일 출생

    // When
    AgeGroup ageGroup = ageCalculator.getAgeGroupFromResidentNumber(residentNumber);

    // Then
    assertEquals(AgeGroup.THIRTIES.getDisplayName(), ageGroup.getDisplayName());
  }

  @Test
  @DisplayName("주민등록번호에서 연령대 직접 계산 - 30대")
  void getAgeGroupFromResidentNumber_Thirties() {
    // Given
    String residentNumber = "9001011234567"; // 1990년 1월 1일 출생

    // When
    AgeGroup ageGroup = ageCalculator.getAgeGroupFromResidentNumber(residentNumber);

    // Then
    assertEquals(AgeGroup.THIRTIES.getDisplayName(), ageGroup.getDisplayName());
  }

  @Test
  @DisplayName("주민등록번호에서 연령대 직접 계산 - 10대")
  void getAgeGroupFromResidentNumber_Teens() {
    // Given
    String residentNumber = "0501013234567"; // 2005년 1월 1일 출생

    // When
    AgeGroup ageGroup = ageCalculator.getAgeGroupFromResidentNumber(residentNumber);

    // Then
    assertEquals(AgeGroup.TWENTIES.getDisplayName(), ageGroup.getDisplayName());
  }

  @Test
  @DisplayName("경계값 테스트 - 10대 경계")
  void getAgeGroup_Boundary_Teens() {
    // Given
    int age9 = 9;
    int age10 = 10;
    int age19 = 19;
    int age20 = 20;

    // When & Then
    assertEquals(AgeGroup.UNDER_10.getDisplayName(), AgeGroup.fromAge(age9).getDisplayName());
    assertEquals(AgeGroup.TEENS.getDisplayName(), AgeGroup.fromAge(age10).getDisplayName());
    assertEquals(AgeGroup.TEENS.getDisplayName(), AgeGroup.fromAge(age19).getDisplayName());
    assertEquals(AgeGroup.TWENTIES.getDisplayName(), AgeGroup.fromAge(age20).getDisplayName());
  }

  @Test
  @DisplayName("경계값 테스트 - 20대 경계")
  void getAgeGroup_Boundary_Twenties() {
    // Given
    int age19 = 19;
    int age20 = 20;
    int age29 = 29;
    int age30 = 30;

    // When & Then
    assertEquals(AgeGroup.TEENS.getDisplayName(), AgeGroup.fromAge(age19).getDisplayName());
    assertEquals(AgeGroup.TWENTIES.getDisplayName(), AgeGroup.fromAge(age20).getDisplayName());
    assertEquals(AgeGroup.TWENTIES.getDisplayName(), AgeGroup.fromAge(age29).getDisplayName());
    assertEquals(AgeGroup.THIRTIES.getDisplayName(), AgeGroup.fromAge(age30).getDisplayName());
  }

  @Test
  @DisplayName("잘못된 주민등록번호 길이 - 예외 발생")
  void calculateAge_InvalidLength_ThrowsException() {
    // Given
    String invalidResidentNumber = "123456789"; // 9자리

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
      ageCalculator.calculateAge(invalidResidentNumber);
    });
  }

  @Test
  @DisplayName("잘못된 주민등록번호 길이 - null")
  void calculateAge_Null_ThrowsException() {
    // Given
    String nullResidentNumber = null;

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
      ageCalculator.calculateAge(nullResidentNumber);
    });
  }

  @Test
  @DisplayName("잘못된 성별 코드 - 예외 발생")
  void calculateAge_InvalidGenderCode_ThrowsException() {
    // Given
    String invalidGenderCode = "9001015234567"; // 성별 코드 5

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
      ageCalculator.calculateAge(invalidGenderCode);
    });
  }

  @Test
  @DisplayName("잘못된 성별 코드 - 0")
  void calculateAge_InvalidGenderCodeZero_ThrowsException() {
    // Given
    String invalidGenderCode = "9001010234567"; // 성별 코드 0

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
      ageCalculator.calculateAge(invalidGenderCode);
    });
  }

  @Test
  @DisplayName("잘못된 성별 코드 - 5 이상")
  void calculateAge_InvalidGenderCodeFive_ThrowsException() {
    // Given
    String invalidGenderCode = "9001015234567"; // 성별 코드 5

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
      ageCalculator.calculateAge(invalidGenderCode);
    });
  }

  @Test
  @DisplayName("다양한 출생년도 테스트")
  void calculateAge_VariousBirthYears() {
    // Given & When & Then
    // 1900년대 출생
    assertEquals(35, ageCalculator.calculateAge("9001011234567")); // 1990년
    assertEquals(45, ageCalculator.calculateAge("8001011234567")); // 1980년
    assertEquals(55, ageCalculator.calculateAge("7001011234567")); // 1970년

    // 2000년대 출생
    assertEquals(25, ageCalculator.calculateAge("0001013234567")); // 2000년
    assertEquals(15, ageCalculator.calculateAge("1001013234567")); // 2010년
    assertEquals(5, ageCalculator.calculateAge("2001013234567")); // 2020년
  }

  @Test
  @DisplayName("매우 큰 연령 테스트")
  void getAgeGroup_VeryLargeAge() {
    // Given
    int largeAge = 150;

    // When
    AgeGroup ageGroup = AgeGroup.fromAge(largeAge);

    // Then
    assertEquals(AgeGroup.OVER_80.getDisplayName(), ageGroup.getDisplayName());
  }
}