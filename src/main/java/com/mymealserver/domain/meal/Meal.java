package com.mymealserver.domain.meal;

import com.mymealserver.common.db.SoftDeletable;
import com.mymealserver.common.enums.AnalysisStatus;
import com.mymealserver.common.enums.MealType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false)
    @Builder.Default
    private Boolean notificationSent = false;

    public void updateAnalysisStatus(AnalysisStatus status) {
        this.analysisStatus = status;
    }

    public boolean isAnalysisCompleted() {
        return analysisStatus == AnalysisStatus.COMPLETED;
    }

    public void markNotificationSent() {
        this.notificationSent = true;
    }

    public boolean isNotificationSent() {
        return notificationSent;
    }

    public void updatePhoto(String photoUrl, String photoKey) {
        this.photoUrl = photoUrl;
        this.photoKey = photoKey;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
