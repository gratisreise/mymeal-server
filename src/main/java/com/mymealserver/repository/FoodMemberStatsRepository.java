package com.mymealserver.repository;

import com.mymealserver.entity.FoodMemberStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodMemberStatsRepository extends JpaRepository<FoodMemberStats, Long> {
}
