package com.mymealserver.domain.meallog;

import com.mymealserver.common.db.SoftDeletable;
import com.mymealserver.domain.meal.Meal;
import com.mymealserver.domain.mealanalysis.MealAnalysis;
import com.mymealserver.domain.reaction.Reaction;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

  @Column(columnDefinition = "vector(3072)")
  private String embedding;

  @Column private LocalDateTime embeddingCreatedAt;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

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

  private static String buildMealSummary(Meal meal, MealAnalysis analysis) {
    return String.format(
        "%s, %s, %dkcal, 탄수화물 %.0fg, 단백질 %.0fg, 지방 %.0fg",
        analysis.getMealName(),
        meal.getMealType().getDescription(),
        Math.round(analysis.getCalories()),
        analysis.getCarbohydrates(),
        analysis.getProtein(),
        analysis.getFat());
  }

  private static String buildReactionSummary(Reaction reaction) {
    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format(
            "소화 상태: %d/5, 포만감: %d/5, 에너지: %d/5",
            reaction.getDigestionLevel(), reaction.getFullnessLevel(), reaction.getEnergyLevel()));

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
