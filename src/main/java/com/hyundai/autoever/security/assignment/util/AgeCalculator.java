package com.hyundai.autoever.security.assignment.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AgeCalculator {

  /**
   * 주민등록번호에서 연령을 계산합니다.
   */
  public int calculateAge(String residentNumber) {
    if (residentNumber == null || residentNumber.length() != 13) {
      throw new IllegalArgumentException("주민등록번호는 13자리여야 합니다.");
    }

    String birthDateStr = residentNumber.substring(0, 6);
    int genderCode = Integer.parseInt(residentNumber.substring(6, 7));

    // 생년월일 파싱
    int year = Integer.parseInt(birthDateStr.substring(0, 2));
    int month = Integer.parseInt(birthDateStr.substring(2, 4));
    int day = Integer.parseInt(birthDateStr.substring(4, 6));

    // 성별 코드로 출생년도 결정
    if (genderCode == 1 || genderCode == 2) {
      year += 1900; // 1900년대 출생
    } else if (genderCode == 3 || genderCode == 4) {
      year += 2000; // 2000년대 출생
    } else {
      throw new IllegalArgumentException("잘못된 성별 코드입니다.");
    }

    LocalDate birthDate = LocalDate.of(year, month, day);
    LocalDate currentDate = LocalDate.now();

    return currentDate.getYear() - birthDate.getYear();
  }

  /**
   * 연령을 기반으로 연령대를 반환합니다.
   */
  public String getAgeGroup(int age) {
    if (age < 10)
      return "10대 미만";
    if (age < 20)
      return "10대";
    if (age < 30)
      return "20대";
    if (age < 40)
      return "30대";
    if (age < 50)
      return "40대";
    if (age < 60)
      return "50대";
    if (age < 70)
      return "60대";
    if (age < 80)
      return "70대";
    return "80대 이상";
  }

  /**
   * 주민등록번호에서 연령대를 직접 계산합니다.
   */
  public String getAgeGroupFromResidentNumber(String residentNumber) {
    int age = calculateAge(residentNumber);
    return getAgeGroup(age);
  }
}