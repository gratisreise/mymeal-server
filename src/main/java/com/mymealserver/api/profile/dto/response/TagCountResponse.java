package com.mymealserver.api.profile.dto.response;

import lombok.Builder;

@Builder
public record TagCountResponse(String tag, Integer count) {

  public static TagCountResponse of(String tag, Integer count) {
    return TagCountResponse.builder().tag(tag).count(count).build();
  }
}
