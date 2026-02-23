package com.mymealserver.domain.food;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    /**
     * 음식 이름으로 조회
     */
    Optional<Food> findByName(String name);
}
