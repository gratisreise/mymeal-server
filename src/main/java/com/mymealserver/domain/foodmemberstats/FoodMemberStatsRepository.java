package com.mymealserver.domain.foodmemberstats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodMemberStatsRepository extends JpaRepository<FoodMemberStats, Long> {

    /**
     * Find all food stats for a member
     */
    List<FoodMemberStats> findByMemberId(Long memberId);

    /**
     * Get tag statistics for a member
     * Returns aggregated data: tag name, average score, and meal count
     * This is a simplified version - in production, you'd want a proper tags table
     *
     * NOTE: This is a placeholder implementation. The actual tag statistics
     * require a more complex query joining with the food_tags table.
     * For now, this returns food-level statistics which can be used as a proxy.
     */
    @Query("""
        SELECT fms.memberId as memberId,
               f.name as tagName,
               fms.averageScore as averageScore,
               fms.mealCount as count
        FROM FoodMemberStats fms
        JOIN Food f ON fms.foodId = f.id
        WHERE fms.memberId = :memberId
        ORDER BY fms.mealCount DESC
    """)
    List<Object[]> findTagStatistics(@Param("memberId") Long memberId);
}
