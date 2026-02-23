package com.mymealserver.external.batch.writer;

import com.mymealserver.domain.recommendation.RecommendationWriter;
import com.mymealserver.domain.recommendation.Recommendation;
import com.mymealserver.external.redis.service.RecommendationNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationItemWriter implements ItemWriter<List<Recommendation>> {

    private final RecommendationWriter recommendationWriter;
    private final RecommendationNotificationService recommendationNotificationService;

    @Override
    public void write(Chunk<? extends List<Recommendation>> chunk) throws Exception {
        log.info("Writing recommendations to database and scheduling in Redis");

        int totalRecommendations = 0;

        for (List<Recommendation> recommendations : chunk) {
            if (recommendations != null && !recommendations.isEmpty()) {
                try {
                    // Save all recommendations for this member
                    List<Recommendation> saved = recommendationWriter.saveAll(recommendations);
                    totalRecommendations += saved.size();

                    log.debug("Saved {} recommendations for memberId: {}",
                            saved.size(), recommendations.get(0).getMemberId());

                    // Schedule notifications in Redis
                    recommendationNotificationService.scheduleNotifications(saved);

                } catch (Exception e) {
                    log.error("Failed to save/schedule recommendations for memberId: {}",
                            recommendations.get(0).getMemberId(), e);
                    throw e;
                }
            }
        }

        log.info("Successfully wrote and scheduled {} recommendations", totalRecommendations);
    }
}
