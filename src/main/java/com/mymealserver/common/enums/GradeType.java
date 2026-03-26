package com.mymealserver.common.enums;

import lombok.Getter;

@Getter
public enum GradeType {
  GOOD("좋음"),
  NORMAL("보통"),
  BAD("나쁨");

  private final String description;

  GradeType(String description) {
    this.description = description;
  }
}
