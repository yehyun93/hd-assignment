package com.hyundai.autoever.security.assignment.util;

import com.hyundai.autoever.security.assignment.enums.AgeGroup;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AgeCalculator {
  public int calculateAge(String residentNumber) {
    if (residentNumber == null || residentNumber.length() != 13) {
      throw new IllegalArgumentException("주민등록번호는 13자리여야 합니다.");
    }

    String birthDateStr = residentNumber.substring(0, 6);
    int genderCode = Integer.parseInt(residentNumber.substring(6, 7));

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

  public AgeGroup getAgeGroupFromResidentNumber(String residentNumber) {
    int age = calculateAge(residentNumber);
    return AgeGroup.fromAge(age);
  }
}