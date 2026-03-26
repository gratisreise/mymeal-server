package com.mymealserver.external.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymealserver.common.enums.NotificationType;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;

@Builder
public record NotificationPayload(
    NotificationType type,
    Long memberId,
    Long targetId,
    String title,
    String body,
    Map<String, String> data) {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static NotificationPayload forRecommendation(
      Long memberId, Long recommendationId, String pushMessage) {
    Map<String, String> data = new HashMap<>();
    data.put("redirectTo", "recommendations");
    data.put("type", "RECOMMENDATION");
    data.put("recommendationId", String.valueOf(recommendationId));

    return NotificationPayload.builder()
        .type(NotificationType.RECOMMENDATION)
        .memberId(memberId)
        .targetId(recommendationId)
        .title("🍽️ 오늘의 식단 추천")
        .body(pushMessage)
        .data(data)
        .build();
  }

  public static NotificationPayload forReactionReminder(Long memberId, Long mealId) {
    Map<String, String> data = new HashMap<>();
    data.put("mealId", String.valueOf(mealId));
    data.put("redirectTo", "reaction");
    data.put("type", "REACTION_REMINDER");

    return NotificationPayload.builder()
        .type(NotificationType.REACTION_REMINDER)
        .memberId(memberId)
        .targetId(mealId)
        .title("식후 반응 기록하기")
        .body("식사 후 30분이 지났습니다. 현재 컨디션이 어떠신가요?")
        .data(data)
        .build();
  }

  public String toJson() {
    try {
      return OBJECT_MAPPER.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("메세지 직렬화 실패", e);
    }
  }

  public static NotificationPayload fromJson(String json) {
    try {
      return OBJECT_MAPPER.readValue(json, NotificationPayload.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("메세지 역직렬화 실패", e);
    }
  }
}
