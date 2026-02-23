package com.mymealserver.domain.reaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    boolean existsByMealId(Long mealId);
    Optional<Reaction> findByMealId(Long mealId);
    List<Reaction> findAllByMealIdIn(List<Long> mealIds);

    /**
     * Count reactions for given meal IDs
     */
    long countByMealIdIn(List<Long> mealIds);

    /**
     * Find all reactions for a member by joining with Meal table
     */
    @Query("SELECT r FROM Reaction r JOIN Meal m ON r.mealId = m.id " +
           "WHERE m.memberId = :memberId AND m.deletedAt IS NULL")
    List<Reaction> findByMemberId(@Param("memberId") Long memberId);

    /**
     * Find reactions for a member within a date range
     */
    @Query("SELECT r FROM Reaction r JOIN Meal m ON r.mealId = m.id " +
           "WHERE m.memberId = :memberId " +
           "AND m.mealTime BETWEEN :startDate AND :endDate " +
           "AND m.deletedAt IS NULL")
    List<Reaction> findByMemberIdAndDateRange(
        @Param("memberId") Long memberId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Calculate average score for all reactions of a member
     */
    @Query("SELECT AVG(r.overallScore) FROM Reaction r " +
           "JOIN Meal m ON r.mealId = m.id " +
           "WHERE m.memberId = :memberId AND m.deletedAt IS NULL")
    Double calculateAverageScoreByMemberId(@Param("memberId") Long memberId);
}
