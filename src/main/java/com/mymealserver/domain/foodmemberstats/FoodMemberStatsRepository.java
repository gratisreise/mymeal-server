package com.mymealserver.domain.foodmemberstats;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodMemberStatsRepository extends JpaRepository<FoodMemberStats, Long> {

  List<FoodMemberStats> findByMemberId(Long memberId);

  @Query(
      """
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
