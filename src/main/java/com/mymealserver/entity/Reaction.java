package com.mymealserver.entity;

import com.mymealserver.entity.enums.GradeType;
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
public class Reaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long mealId;

    @Column(nullable = false)
    private Short digestionLevel;

    @Column(nullable = false)
    private Short fullnessLevel;

    @Column(nullable = false)
    private Short energyLevel;

    @Column(nullable = false)
    @Builder.Default
    private Boolean hasHeartburn = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean hasGas = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean hasBloating = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean hasHeadache = false;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column
    private Double overallScore;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GradeType grade;

    public void calculateOverallScore() {
        double total = digestionLevel + fullnessLevel + energyLevel;
        double penalty = 0;

        if (hasHeartburn) penalty -= 1;
        if (hasGas) penalty -= 0.5;
        if (hasBloating) penalty -= 0.5;
        if (hasHeadache) penalty -= 1;

        double score = Math.max(1, Math.min(5, total + penalty));
        this.overallScore = Math.round(score * 100.0) / 100.0;

        if (this.overallScore >= 4) {
            this.grade = GradeType.GOOD;
        } else if (this.overallScore >= 2.5) {
            this.grade = GradeType.NORMAL;
        } else {
            this.grade = GradeType.BAD;
        }
    }
}
