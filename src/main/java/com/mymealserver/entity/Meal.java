package com.mymealserver.entity;

import com.mymealserver.entity.enums.MealType;
import com.mymealserver.entity.enums.AnalysisStatus;
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
public class Meal extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MealType mealType;

    @Column(nullable = false)
    private LocalDateTime mealTime;

    @Column(nullable = false, length = 500)
    private String photoUrl;

    @Column(nullable = false, length = 500)
    private String photoKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AnalysisStatus analysisStatus = AnalysisStatus.PENDING;

    @Column(length = 500)
    private String memo;

    public void updateAnalysisStatus(AnalysisStatus status) {
        this.analysisStatus = status;
    }

    public boolean isAnalysisCompleted() {
        return analysisStatus == AnalysisStatus.COMPLETED;
    }
}
