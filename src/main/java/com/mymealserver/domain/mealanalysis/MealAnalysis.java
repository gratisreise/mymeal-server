package com.mymealserver.domain.mealanalysis;

import com.mymealserver.common.db.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealAnalysis extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private Long mealId;

  @Column private Long foodId;

  @Column(length = 100)
  private String mealName;

  @Column private Double calories;

  @Column private Double carbohydrates;

  @Column private Double protein;

  @Column private Double fat;

  @Column private Double confidence;

  @Column(columnDefinition = "jsonb")
  private String rawResponse;

  @Transient private Double score;
}
