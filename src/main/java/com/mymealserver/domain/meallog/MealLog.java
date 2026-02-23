package com.mymealserver.domain.MealLog;

import com.mymealserver.domain.MealAnalysis.MealAnalysis;
import com.mymealserver.domain.base.SoftDeletable;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.reaction.Reaction;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MealLog extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long mealId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mealSummary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reactionSummary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String combinedSummary;

    @Column(columnDefinition = "vector(1536)")
    private Float[] embedding;

    @Column
    private LocalDateTime embeddingCreatedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Meal + MealAnalysis + Reaction 정보로 MealLog 생성
     * Reaction 생성 시점에 호출됨
     */
    public static MealLog from(Meal meal, MealAnalysis analysis, Reaction reaction) {
        String mealSummary = buildMealSummary(meal, analysis);
        String reactionSummary = buildReactionSummary(reaction);
        String combinedSummary = mealSummary + "\n" + reactionSummary;

        return MealLog.builder()
                .mealId(meal.getId())
                .memberId(meal.getMemberId())
                .mealSummary(mealSummary)
                .reactionSummary(reactionSummary)
                .combinedSummary(combinedSummary)
                .embedding(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 임베딩 벡터 설정
     */
    public void setEmbedding(Float[] embedding) {
        this.embedding = embedding;
        this.embeddingCreatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Meal + MealAnalysis 정보로 식사 요약 생성
     * 예: "김치찌개, 점심, 450kcal, 탄수화물 45g, 단백질 25g, 지방 15g"
     */
    private static String buildMealSummary(Meal meal, MealAnalysis analysis) {
        return String.format("%s, %s, %dkcal, 탄수화물 %.0fg, 단백질 %.0fg, 지방 %.0fg",
                analysis.getMealName(),
                meal.getMealType().getDescription(),
                Math.round(analysis.getCalories()),
                analysis.getCarbohydrates(),
                analysis.getProtein(),
                analysis.getFat());
    }

    /**
     * Reaction 정보로 반응 요약 생성
     * 예: "소화 상태: 4/5, 포만감: 3/5, 에너지: 5/5. 속쓰림 있음, 가스 없음, 복부 팽만 없음, 두통 없음. 메모: 컨디션 양호"
     */
    private static String buildReactionSummary(Reaction reaction) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("소화 상태: %d/5, 포만감: %d/5, 에너지: %d/5",
                reaction.getDigestionLevel(),
                reaction.getFullnessLevel(),
                reaction.getEnergyLevel()));

        List<String> symptoms = new ArrayList<>();
        if (reaction.getHasHeartburn()) symptoms.add("속쓰림");
        if (reaction.getHasGas()) symptoms.add("가스");
        if (reaction.getHasBloating()) symptoms.add("복부 팽만");
        if (reaction.getHasHeadache()) symptoms.add("두통");

        if (!symptoms.isEmpty()) {
            sb.append(". ");
            for (int i = 0; i < symptoms.size(); i++) {
                sb.append(symptoms.get(i));
                if (i < symptoms.size() - 1) {
                    sb.append(" 있음, ");
                } else {
                    sb.append(" 있음.");
                }
            }
        }

        if (reaction.getMemo() != null && !reaction.getMemo().isBlank()) {
            sb.append(" 메모: ").append(reaction.getMemo());
        }

        return sb.toString();
    }
}
