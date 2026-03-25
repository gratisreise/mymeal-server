package com.mymealserver.domain.meal;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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



    List<Meal> findByMemberIdAndMealTimeBetweenAndDeletedAtIsNull(
        Long memberId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    long countByMemberIdAndDeletedAtIsNull(Long memberId);

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
