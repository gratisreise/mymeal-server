package com.mymealserver.common.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
  RECOMMENDATION("식사 추천"),
  REACTION_REMINDER("리액션 알림"),
  MEAL_REMINDER("식사 알림");

  private final String description;

  NotificationType(String description) {
    this.description = description;
  }
}
