package com.mymealserver.repository;

import com.mymealserver.entity.Recommendation;
import com.mymealserver.entity.enums.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findByMemberIdAndDeletedAtIsNullOrderByScheduledTimeAsc(Long memberId);

    List<Recommendation> findByMemberIdAndMealTypeAndDeletedAtIsNullOrderByScheduledTimeDesc(
            Long memberId,
            MealType mealType
    );

    @Query("""
            SELECT r FROM Recommendation r
            WHERE r.memberId = :memberId
            AND r.scheduledTime >= :startDate
            AND r.scheduledTime < :endDate
            AND r.deletedAt IS NULL
            ORDER BY r.scheduledTime ASC
            """)
    List<Recommendation> findByMemberIdAndDateRange(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
            SELECT r FROM Recommendation r
            WHERE r.isSent = false
            AND r.scheduledTime <= :scheduledTime
            AND r.deletedAt IS NULL
            ORDER BY r.scheduledTime ASC
            """)
    List<Recommendation> findPendingRecommendations(
            @Param("scheduledTime") LocalDateTime scheduledTime
    );

    boolean existsByMemberIdAndMealTypeAndScheduledTimeBetweenAndDeletedAtIsNull(
            Long memberId,
            MealType mealType,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    @Query("""
            SELECT COUNT(r) > 0 FROM Recommendation r
            WHERE r.memberId = :memberId
            AND r.mealType = :mealType
            AND DATE(r.scheduledTime) = CURRENT_DATE
            AND r.deletedAt IS NULL
            """)
    boolean existsTodayRecommendation(
            @Param("memberId") Long memberId,
            @Param("mealType") MealType mealType
    );

    void deleteAllByScheduledTimeBefore(LocalDateTime dateTime);
}
