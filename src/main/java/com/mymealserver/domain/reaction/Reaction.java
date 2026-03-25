package com.mymealserver.domain.reaction;

import com.mymealserver.api.reaction.dto.request.ReactionRequest;
import com.mymealserver.common.db.BaseEntity;
import com.mymealserver.common.enums.GradeType;
import jakarta.persistence.*;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
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

    public void update(ReactionRequest request) {
        this.digestionLevel = request.digestionLevel().shortValue();
        this.fullnessLevel = request.fullnessLevel().shortValue();
        this.energyLevel = request.energyLevel().shortValue();
        this.hasHeartburn = Optional.ofNullable(request.hasHeartburn()).orElse(false);
        this.hasGas = Optional.ofNullable(request.hasGas()).orElse(false);
        this.hasBloating = Optional.ofNullable(request.hasBloating()).orElse(false);
        this.hasHeadache = Optional.ofNullable(request.hasHeadache()).orElse(false);
        this.memo = request.memo();
    }
}
