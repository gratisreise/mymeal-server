package com.mymealserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodMemberStats extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long foodId;

    @Column(nullable = false)
    private Double averageScore;

    @Column(nullable = false)
    private Integer mealCount;

    @Column
    private LocalDateTime lastMealAt;

    public void updateStats(double newScore) {
        this.mealCount++;
        this.lastMealAt = LocalDateTime.now();

        if (this.averageScore == null) {
            this.averageScore = newScore;
        } else {
            double currentSum = this.averageScore * (this.mealCount - 1);
            this.averageScore = (currentSum + newScore) / this.mealCount;
        }
    }
}
