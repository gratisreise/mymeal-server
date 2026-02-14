package com.mymealserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Food extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column
    private Double calories;

    @Column
    private Double carbohydrates;

    @Column
    private Double protein;

    @Column
    private Double fat;

    @Column
    private Double averageScore;

    @Column(nullable = false)
    @Builder.Default
    private Integer mealCount = 0;

    @ElementCollection
    @CollectionTable(name = "food_tags", joinColumns = @JoinColumn(name = "foodId"))
    @Column(name = "tag", length = 50)
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new HashSet<>();
        }
        this.tags.add(tag);
    }

    public void removeTag(String tag) {
        if (this.tags != null) {
            this.tags.remove(tag);
        }
    }

    public void incrementMealCount() {
        this.mealCount = (this.mealCount == null ? 0 : this.mealCount) + 1;
    }

    public void updateAverageScore(double newScore) {
        if (this.averageScore == null) {
            this.averageScore = newScore;
        } else {
            double currentSum = this.averageScore * this.mealCount;
            this.averageScore = (currentSum + newScore) / (this.mealCount + 1);
        }
    }
}
