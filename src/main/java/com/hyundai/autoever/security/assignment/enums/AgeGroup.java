package com.hyundai.autoever.security.assignment.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum AgeGroup {
  UNDER_10("UNDER_10", 0, 9, "10대 미만"),
  TEENS("TEENS", 10, 19, "10대"),
  TWENTIES("TWENTIES", 20, 29, "20대"),
  THIRTIES("THIRTIES", 30, 39, "30대"),
  FORTIES("FORTIES", 40, 49, "40대"),
  FIFTIES("FIFTIES", 50, 59, "50대"),
  SIXTIES("SIXTIES", 60, 69, "60대"),
  SEVENTIES("SEVENTIES", 70, 79, "70대"),
  OVER_80("OVER_80", 80, 150, "80대 이상");

  private final String code;
  private final int minAge;
  private final int maxAge;
  private final String displayName;

  AgeGroup(String code, int minAge, int maxAge, String displayName) {
    this.code = code;
    this.minAge = minAge;
    this.maxAge = maxAge;
    this.displayName = displayName;
  }

  public static AgeGroup fromAge(int age) {
    return Arrays.stream(values())
        .filter(group -> age >= group.minAge && age <= group.maxAge)
        .findFirst()
        .orElse(OVER_80);
  }

  @JsonCreator
  public static AgeGroup fromCode(String code) {
    return Arrays.stream(values())
        .filter(group -> group.code.equalsIgnoreCase(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Invalid age group code: " + code));
  }

  @JsonValue
  public String getCode() {
    return code;
  }

  public boolean contains(int age) {
    return age >= minAge && age <= maxAge;
  }
}