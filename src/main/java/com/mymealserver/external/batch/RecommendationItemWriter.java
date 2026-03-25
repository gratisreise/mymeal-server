package com.mymealserver.external.batch;

import com.mymealserver.domain.recommendation.Recommendation;
import com.mymealserver.domain.recommendation.RecommendationWriter;
import com.mymealserver.external.redis.NotificationPayload;
import com.mymealserver.external.redis.UnifiedNotificationService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecommendationItemWriter implements ItemWriter<List<Recommendation>> {

    private static final int NOTIFICATION_ADVANCE_MINUTES = 30;

    private final RecommendationWriter recommendationWriter;
    private final UnifiedNotificationService unifiedNotificationService;

    @Override
    public void write(Chunk<? extends List<Recommendation>> chunk) {
        for (List<Recommendation> recommendations : chunk) {
            if (recommendations != null && !recommendations.isEmpty()) {
                List<Recommendation> saved = recommendationWriter.saveAll(recommendations);

                for (Recommendation recommendation : saved) {
                    scheduleRecommendationNotification(recommendation);
                }
            }
        }
    }

    private void scheduleRecommendationNotification(Recommendation recommendation) {
        LocalDateTime notificationTime = recommendation.getScheduledTime()
                .minusMinutes(NOTIFICATION_ADVANCE_MINUTES);

        NotificationPayload payload = NotificationPayload.forRecommendation(
                recommendation.getMemberId(),
                recommendation.getId(),
                recommendation.getPushMessage()
        );

        unifiedNotificationService.schedule(payload, notificationTime);
    }
}
