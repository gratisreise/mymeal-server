package com.mymealserver.service.recommendation;

import com.mymealserver.domain.meal.MealReader;
import com.mymealserver.domain.mealanalysis.MealAnalysisReader;
import com.mymealserver.domain.reaction.ReactionReader;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.mealanalysis.MealAnalysis;
import com.mymealserver.domain.reaction.Reaction;
import com.mymealserver.common.enums.GradeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchService {

    private final MealReader mealReader;
    private final MealAnalysisReader mealAnalysisReader;
    private final ReactionReader reactionReader;

    // TODO: Future implementation with pgvector
    // Currently using simple filtering, will be enhanced with vector similarity search
    public List<RagPromptBuilder.MealWithReaction> findSimilarGoodMeals(
            Long memberId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        log.debug("Finding similar good meals for memberId: {}, date range: {} - {}",
                memberId, startDate, endDate);

        // Get meals in date range
        List<Meal> meals = mealReader.findByMemberIdAndDateRange(memberId, startDate, endDate);

        // Get reactions for these meals
        List<Long> mealIds = meals.stream()
                .map(Meal::getId)
                .toList();

        Map<Long, Reaction> reactionMap = reactionReader.findByMealIdsAsMap(mealIds);

        // Get meal analyses
        Map<Long, MealAnalysis> analysisMap = mealIds.stream()
                .map(mealAnalysisReader::findByMealId)
                .filter(Optional::isPresent)
                .collect(Collectors.toMap(
                        analysis -> analysis.get().getMealId(),
                        analysis -> analysis.get()
                ));

        List<RagPromptBuilder.MealWithReaction> result = new ArrayList<>();

        for (Meal meal : meals) {
            Reaction reaction = reactionMap.get(meal.getId());

            // Only include GOOD grade meals
            if (reaction != null && reaction.getGrade() == GradeType.GOOD) {
                MealAnalysis analysis = analysisMap.get(meal.getId());

                result.add(RagPromptBuilder.MealWithReaction.builder()
                        .meal(meal)
                        .mealAnalysis(analysis)
                        .reaction(reaction)
                        .build());
            }
        }

        log.debug("Found {} good meals for recommendation context", result.size());

        return result;
    }

    // TODO: Future implementation with pgvector
    // This method will be enhanced to use vector embeddings for semantic similarity
    public List<RagPromptBuilder.MealWithReaction> findSimilarMealsByEmbedding(
            Long memberId,
            String queryEmbedding,
            int limit
    ) {
        // Future implementation:
        // 1. Generate embedding for query text
        // 2. Use pgvector to find similar meals by cosine similarity
        // 3. Return top-k similar meals with GOOD reactions

        log.warn("Vector similarity search not yet implemented. Using fallback method.");
        return List.of();
    }

    public List<RagPromptBuilder.MealWithReaction> findRecentGoodMeals(
            Long memberId,
            int days
    ) {
        LocalDateTime endDate = LocalDate.now().atStartOfDay();
        LocalDateTime startDate = endDate.minusDays(days);

        return findSimilarGoodMeals(memberId, startDate, endDate);
    }
}
