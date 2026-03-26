package com.mymealserver.common.enums;

import lombok.Getter;

@Getter
public enum PromptType {
  POSITIVE_SEARCH("긍정적 식사 검색");

  private final String description;

  PromptType(String description) {
    this.description = description;
  }
}
