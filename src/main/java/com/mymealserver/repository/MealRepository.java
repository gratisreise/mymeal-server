package com.mymealserver.repository;

import com.mymealserver.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long>, JpaSpecificationExecutor<Meal> {
    boolean existsByMemberId(Long memberId);

    List<Meal> findAllByMemberIdAndMealTimeBetween(
            Long memberId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    List<Meal> findAllByMemberIdAndMealTimeBetweenAndDeletedAtIsNull(
            Long memberId,
            LocalDateTime startOfMonth,
            LocalDateTime endOfMonth
    );

    /**
     * Count total meals for a member (excluding deleted)
     */
    long countByMemberIdAndDeletedAtIsNull(Long memberId);

    /**
     * Find meals by member and date range (excluding deleted)
     */
    List<Meal> findByMemberIdAndMealTimeBetweenAndDeletedAtIsNull(
            Long memberId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * Get weekly trend - average reaction scores per day for the last 7 days
     * This query joins Meal with Reaction to get daily average scores
     */
    @Query("""
        SELECT DATE(m.mealTime) as date,
               COALESCE(AVG(r.overallScore), 0.0) as averageScore
        FROM Meal m
        LEFT JOIN Reaction r ON m.id = r.mealId
        WHERE m.memberId = :memberId
          AND m.mealTime >= :startDate
          AND m.deletedAt IS NULL
        GROUP BY DATE(m.mealTime)
        ORDER BY DATE(m.mealTime)
    """)
    List<Object[]> findWeeklyTrend(@Param("memberId") Long memberId,
                                   @Param("startDate") LocalDateTime startDate);
}
